package com.springernature.e2e

import com.google.gson.Gson
import com.springernature.e2e.Database.maybeRetrieveManuscript
import com.springernature.e2e.Database.retrieveManuscript
import com.springernature.e2e.Database.saveManuscriptToDb
import com.springernature.e2e.Database.transactionIdSequence
import com.springernature.e2e.ManuscriptTable.manuscriptTable
import com.springernature.e2e.TransactionLog.manuscriptId
import com.springernature.e2e.TransactionLog.payload
import com.springernature.e2e.TransactionLog.transactionId
import com.springernature.e2e.TransactionLog.transactionLogTable
import com.springernature.e2e.TransactionLog.transactionType
import org.jooq.Configuration
import org.jooq.impl.DSL
import org.jooq.impl.DSL.`val`
import org.jooq.impl.DSL.select
import org.neo4j.graphdb.GraphDatabaseService
import java.math.BigInteger

data class CreateManuscript(val originalContent: String) : TransactionEvent {
    companion object {
        fun fromJson(json: String): CreateManuscript {
            return Gson().fromJson(json, CreateManuscript::class.java)
        }
    }
}

data class SetMarkupFragment(val fragmentName: String, val fragment: MarkUpFragment) : TransactionEvent {
    companion object {
        fun fromJson(json: String): SetMarkupFragment {
            return Gson().fromJson(json, SetMarkupFragment::class.java)
        }
    }
}

data class SetAuthorsFragment(val authors: Authors) : TransactionEvent {
    companion object {
        fun fromJson(json: String): SetAuthorsFragment {
            return Gson().fromJson(json, SetAuthorsFragment::class.java)
        }
    }
}

fun logEvent(dataContext: Configuration, id: ManuscriptId, event: TransactionEvent) {
    DSL.using(dataContext).insertInto(transactionLogTable, transactionId, transactionType, manuscriptId, payload)
        .values(
            transactionIdSequence.nextval(),
            `val`(event.javaClass.simpleName),
            `val`(id.raw),
            `val`(event.toJson())).execute()
}

fun processEvents(configuration: Configuration, graphDbInTransaction: GraphDatabaseService) {
    var biggestTransactionId = java.math.BigInteger.valueOf(-1)

    println(DSL.using(configuration).select(ProcessedEvent.transactionId).from(ProcessedEvent.processedEventTable).fetchAny())

    DSL.using(configuration)
        .select(manuscriptId, transactionId, payload, transactionType)
        .from(transactionLogTable)
        .where(
            transactionId.gt(select(ProcessedEvent.transactionId).from(ProcessedEvent.processedEventTable))
        )
        .orderBy(transactionId)
        .fetchMany()
        .forEach({ result ->
            result.forEach({ record ->
                val id = ManuscriptId(record[TransactionLog.manuscriptId])
                @Suppress("CAST_NEVER_SUCCEEDS")
                val transaction: java.math.BigInteger = (record[transactionId] as java.math.BigDecimal).toBigInteger() // jooq bug?
                println("Processing transaction $transaction")

                when (record[transactionType]) {
                    "CreateManuscript" -> {
                        if (maybeRetrieveManuscript(configuration, id) == null) {
                            val event = CreateManuscript.Companion.fromJson(record[payload])
                            val manuscript = Manuscript.Companion.EMPTY(id).copy(
                                content = MarkUpFragment(MarkUp(event.originalContent), false, null),
                                originalContent = MarkUp(event.originalContent))
                            DSL.using(configuration)
                                .insertInto(manuscriptTable, ManuscriptTable.manuscriptId, ManuscriptTable.payload)
                                .values(id.raw, manuscript.toJson())
                                .execute()
                            manuscript.saveNode(graphDbInTransaction)

                        }
                    }
                    "SetMarkupFragment" -> {
                        val event = SetMarkupFragment.Companion.fromJson(record[payload])
                        val manuscript = retrieveManuscript(configuration, id)
                        saveManuscriptToDb(
                            configuration,
                            updateContentFrom(event.updateManuscript(manuscript)))
                        manuscript.saveNode(graphDbInTransaction)
                    }
                    "SetAuthorsFragment" -> {
                        val event = SetAuthorsFragment.Companion.fromJson(record[payload])
                        val manuscript = retrieveManuscript(configuration, id)
                        saveManuscriptToDb(
                            configuration,
                            updateContentFrom(event.updateManuscript(manuscript)))
                        manuscript.saveNode(graphDbInTransaction)
                        extractAuthors(manuscript, event)
                    }
                    else -> throw RuntimeException("Don't understand transaction type: " + record[transactionType])
                }
                biggestTransactionId = transaction
            })
        })
    if (biggestTransactionId.compareTo(BigInteger.ZERO) >= 0) {
        println("have processed transactions up to $biggestTransactionId")
        DSL.using(configuration).update(ProcessedEvent.processedEventTable)
            .set(ProcessedEvent.transactionId, biggestTransactionId)
            .execute()
        println(DSL.using(configuration).select(ProcessedEvent.transactionId).from(ProcessedEvent.processedEventTable).fetchAny())
    }
}

fun extractAuthors(manuscript: Manuscript, event: SetAuthorsFragment) {
    manuscript.originalContent.extract(event.authors.originalDocumentLocation!!)
        .map { line -> line.replace("<[^>]*>".toRegex(), "") }
        .flatMap { line -> line.split("(([0-9]+,)*[0-9]+)?,[^0-9]".toRegex()) }
        .forEach { println(">>>>>3 " + it) }
}

private fun MarkUp.extract(range: IntRange): List<String> =
    this.raw.lines().filter({ line ->
        range.contains(Integer.parseInt(
            "data-index=\"([0-9]*)\"".toRegex().find(line)?.groups?.get(1)?.value ?: "-1"
        ))
    })
//                    { acc2, index -> acc2.replace("data-index=\"$index\"", "data-index=\"$index\" data-already-used") })

private fun SetAuthorsFragment.updateManuscript(manuscript: Manuscript): Manuscript =
    manuscript.copy(authors = this.authors)

fun SetMarkupFragment.updateManuscript(manuscript: Manuscript): Manuscript =
    when (fragmentName) {
        "title" -> manuscript.copy(title = fragment)
        "abstract" -> manuscript.copy(abstract = fragment)
        else -> throw RuntimeException("Don't understand fragment name: " + fragmentName)
    }