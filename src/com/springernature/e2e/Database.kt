package com.springernature.e2e

import com.springernature.e2e.ProcessedEvent.processedEvent
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.SQLDialect
import org.jooq.Table
import org.jooq.impl.DSL.*
import java.math.BigInteger
import java.sql.Connection
import java.util.*

object TransactionLog {
    val transactionLog = table("transactionLog")!!
    val transactionId = field(transactionLog.fieldNamed("tranId"), BigInteger::class.java)!!
    val transactionType = field(transactionLog.fieldNamed("type"), String::class.java)!!
    val payload = field(transactionLog.fieldNamed("payload"), String::class.java)!!
    val manuscriptId = field(transactionLog.fieldNamed("manId"), UUID::class.java)!!
}

object ProcessedEvent {
    val processedEvent = table("processedEvent")!!
    val transactionId = field(processedEvent.fieldNamed("tranId"), BigInteger::class.java)!!

}

@Suppress("unused")
fun Table<Record>.fieldNamed(fieldName: String) = name(fieldName) // name("public", name, fieldName)

object Database {


    val manuscript = table("manuscript")!!
    val manuscriptId = field(name("manId"), UUID::class.java)!!
    val payload = field("payload", String::class.java)!!

    val transactionIdSequence = sequence("transactionid_seq")

    fun createDbTables(conn: Connection) {
        val dataContext = using(conn, SQLDialect.H2)

        dataContext
            .createTableIfNotExists(TransactionLog.transactionLog)
            .column(TransactionLog.transactionId)
            .column(TransactionLog.transactionType)
            .column(TransactionLog.manuscriptId)
            .column(TransactionLog.payload)
            .constraint(
                constraint("transactionlog_pk").primaryKey(TransactionLog.transactionId)
            )
            .execute()

        dataContext.createSequenceIfNotExists(transactionIdSequence)
            .execute()

        dataContext
            .createTableIfNotExists(processedEvent)
            .column(ProcessedEvent.transactionId)
            .execute()

        dataContext.deleteFrom(processedEvent).execute()
        dataContext.insertInto(processedEvent, ProcessedEvent.transactionId).values(BigInteger.valueOf(-1))
            .execute()

        dataContext
            .createTableIfNotExists(manuscript)
            .column(manuscriptId)
            .column(payload)
            .execute()
    }

    fun retrieveManuscript(dataContext: DSLContext, id: ManuscriptId): Manuscript {
        return maybeRetrieveManuscript(dataContext, id) ?: throw RuntimeException("not found")
    }

    fun maybeRetrieveManuscript(dataContext: DSLContext, id: ManuscriptId): Manuscript? {
        val record = dataContext
            .select(payload)
            .from(manuscript)
            .where(manuscriptId.eq(id.raw)).fetchOne()
        return record
            ?.let {
                val result = Manuscript.fromJson(it.value1())
                println(">>>>>>>>>> result = $result")
                result
            }

    }

    private fun intRangeFromDbFields(start: Int?, end: Int?) =
        if (start == null || end == null) null else IntRange(start, end)

    fun saveManuscriptToDb(dataContext: DSLContext, manuscript: Manuscript) {
        dataContext.update(Database.manuscript)
            .set(Database.payload, manuscript.toJson())
            .where(Database.manuscriptId.eq(manuscript.id.raw)).execute()
    }
}


