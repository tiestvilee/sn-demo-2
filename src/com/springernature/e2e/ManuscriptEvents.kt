package com.springernature.e2e

import org.jooq.impl.DSL.`val`
import org.jooq.impl.DSL.select

data class CreateManuscript(val originalContent: String) : com.springernature.e2e.TransactionEvent {
    companion object {
        fun fromJson(json: String): com.springernature.e2e.CreateManuscript {
            return com.google.gson.Gson().fromJson(json, com.springernature.e2e.CreateManuscript::class.java)
        }
    }
}

data class SetMarkupFragment(val id: com.springernature.e2e.ManuscriptId, val fragmentName: String, val fragment: com.springernature.e2e.MarkUpFragment) : com.springernature.e2e.TransactionEvent {
    companion object {
        fun fromJson(json: String): com.springernature.e2e.SetMarkupFragment {
            return com.google.gson.Gson().fromJson(json, com.springernature.e2e.SetMarkupFragment::class.java)
        }
    }
}

fun logEvent(dataContext: org.jooq.DSLContext, id: com.springernature.e2e.ManuscriptId, event: com.springernature.e2e.TransactionEvent) {
    dataContext.insertInto(com.springernature.e2e.TransactionLog.transactionLog, com.springernature.e2e.TransactionLog.transactionId, com.springernature.e2e.TransactionLog.transactionType, com.springernature.e2e.Database.manuscriptId, com.springernature.e2e.TransactionLog.payload)
        .values(
            com.springernature.e2e.Database.transactionIdSequence.nextval(),
            `val`(event.javaClass.simpleName),
            `val`(id.raw),
            `val`(event.toJson())).execute()
}

fun processEvents(dataContext: org.jooq.DSLContext) {
    var biggestTransactionId = java.math.BigInteger.valueOf(-1)
    dataContext
        .select(com.springernature.e2e.Database.manuscriptId, com.springernature.e2e.TransactionLog.transactionId, com.springernature.e2e.TransactionLog.payload, com.springernature.e2e.TransactionLog.transactionType)
        .from(com.springernature.e2e.TransactionLog.transactionLog)
        .where(
            com.springernature.e2e.TransactionLog.transactionId.gt(select(com.springernature.e2e.ProcessedEvent.transactionId).from(com.springernature.e2e.ProcessedEvent.processedEvent))
        )
        .orderBy(com.springernature.e2e.TransactionLog.transactionId)
        .fetchMany()
        .forEach({ result ->
            result.forEach({ record ->
                val id = com.springernature.e2e.ManuscriptId(record[com.springernature.e2e.TransactionLog.manuscriptId])
                when (record[com.springernature.e2e.TransactionLog.transactionType]) {
                    "CreateManuscript" -> {
                        if (com.springernature.e2e.Database.maybeRetrieveManuscript(dataContext, id) == null) {
                            val event = com.springernature.e2e.CreateManuscript.Companion.fromJson(record[com.springernature.e2e.TransactionLog.payload])
                            val manuscript = com.springernature.e2e.Manuscript.Companion.EMPTY(id).copy(content = com.springernature.e2e.MarkUpFragment(com.springernature.e2e.MarkUp(event.originalContent), false, null))
                            dataContext
                                .insertInto(com.springernature.e2e.Database.manuscript, com.springernature.e2e.Database.manuscriptId, com.springernature.e2e.Database.payload)
                                .values(id.raw, manuscript.toJson())
                                .execute()
                        }
                    }
                    "SetMarkupFragment" -> {
                        val event = com.springernature.e2e.SetMarkupFragment.Companion.fromJson(record[com.springernature.e2e.TransactionLog.payload])
                        val manuscript = com.springernature.e2e.Database.retrieveManuscript(dataContext, id)
                        com.springernature.e2e.Database.saveManuscriptToDb(
                            dataContext,
                            com.springernature.e2e.updateContentFrom(event.updateManuscript(manuscript)))
                    }
                    else -> throw RuntimeException("Don't understand transaction type: " + record[com.springernature.e2e.TransactionLog.transactionType])
                }
                @Suppress("CAST_NEVER_SUCCEEDS")
                val bigInteger: java.math.BigInteger = (record.get(com.springernature.e2e.TransactionLog.transactionId) as java.math.BigDecimal).toBigInteger() // jooq bug?
                biggestTransactionId = bigInteger
            })
        })
    dataContext.update(com.springernature.e2e.ProcessedEvent.processedEvent).set(com.springernature.e2e.ProcessedEvent.transactionId, biggestTransactionId)
}

fun com.springernature.e2e.SetMarkupFragment.updateManuscript(manuscript: com.springernature.e2e.Manuscript): com.springernature.e2e.Manuscript {
    val newManuscript = when (fragmentName) {
        "title" -> manuscript.copy(title = fragment)
        "abstract" -> manuscript.copy(abstract = fragment)
        else -> throw RuntimeException("Don't understand fragment name: " + fragmentName)
    }
    return newManuscript
}