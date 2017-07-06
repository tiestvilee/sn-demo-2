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
import org.jooq.DSLContext
import org.jooq.impl.DSL.`val`
import org.jooq.impl.DSL.select
import org.neo4j.graphdb.GraphDatabaseService

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

fun logEvent(dataContext: org.jooq.DSLContext, id: ManuscriptId, event: TransactionEvent) {
    dataContext.insertInto(transactionLogTable, transactionId, transactionType, manuscriptId, payload)
        .values(
            transactionIdSequence.nextval(),
            `val`(event.javaClass.simpleName),
            `val`(id.raw),
            `val`(event.toJson())).execute()
}

fun processEvents(dataContext: DSLContext, graphDbInTransaction: GraphDatabaseService) {
    var biggestTransactionId = java.math.BigInteger.valueOf(-1)
    dataContext
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
                when (record[transactionType]) {
                    "CreateManuscript" -> {
                        if (maybeRetrieveManuscript(dataContext, id) == null) {
                            val event = CreateManuscript.Companion.fromJson(record[payload])
                            val manuscript = Manuscript.Companion.EMPTY(id).copy(
                                    content = MarkUpFragment(MarkUp(event.originalContent), false, null),
                                    originalContent = MarkUp(event.originalContent))
                            dataContext
                                .insertInto(manuscriptTable, ManuscriptTable.manuscriptId, ManuscriptTable.payload)
                                .values(id.raw, manuscript.toJson())
                                .execute()
                            manuscript.saveNode(graphDbInTransaction)

                        }
                    }
                    "SetMarkupFragment" -> {
                        val event = SetMarkupFragment.Companion.fromJson(record[payload])
                        val manuscript = retrieveManuscript(dataContext, id)
                        saveManuscriptToDb(
                            dataContext,
                            updateContentFrom(event.updateManuscript(manuscript)))
                        manuscript.saveNode(graphDbInTransaction)
                    }
                    "SetAuthorsFragment" -> {
                        val event = SetAuthorsFragment.Companion.fromJson(record[payload])
                        val manuscript = retrieveManuscript(dataContext, id)
                        saveManuscriptToDb(
                            dataContext,
                            updateContentFrom(event.updateManuscript(manuscript)))
                        manuscript.saveNode(graphDbInTransaction)
                        extractAuthors(manuscript, event)
                    }
                    else -> throw RuntimeException("Don't understand transaction type: " + record[transactionType])
                }
                @Suppress("CAST_NEVER_SUCCEEDS")
                val bigInteger: java.math.BigInteger = (record.get(transactionId) as java.math.BigDecimal).toBigInteger() // jooq bug?
                biggestTransactionId = bigInteger
            })
        })
    dataContext.update(ProcessedEvent.processedEventTable).set(ProcessedEvent.transactionId, biggestTransactionId)
}

fun extractAuthors(manuscript: Manuscript, event: SetAuthorsFragment) {
}

private fun SetAuthorsFragment.updateManuscript(manuscript: Manuscript): Manuscript =
    manuscript.copy(authors = this.authors)

fun SetMarkupFragment.updateManuscript(manuscript: Manuscript): Manuscript =
    when (fragmentName) {
        "title" -> manuscript.copy(title = fragment)
        "abstract" -> manuscript.copy(abstract = fragment)
        else -> throw RuntimeException("Don't understand fragment name: " + fragmentName)
    }