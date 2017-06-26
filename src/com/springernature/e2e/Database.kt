package com.springernature.e2e

import com.springernature.e2e.ProcessedEvent.processedEventTable
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.SQLDialect
import org.jooq.Table
import org.jooq.impl.DSL.*
import java.math.BigInteger
import java.sql.Connection
import java.util.*

object TransactionLog {
    val transactionLogTable = table("transactionLog")!!
    val transactionId = field(transactionLogTable.fieldNamed("tranId"), BigInteger::class.java)!!
    val transactionType = field(transactionLogTable.fieldNamed("type"), String::class.java)!!
    val payload = field(transactionLogTable.fieldNamed("payload"), String::class.java)!!
    val manuscriptId = field(transactionLogTable.fieldNamed("manId"), UUID::class.java)!!
}

object ProcessedEvent {
    val processedEventTable = table("processedEvent")!!
    val transactionId = field(processedEventTable.fieldNamed("tranId"), BigInteger::class.java)!!

}

@Suppress("unused")
fun Table<Record>.fieldNamed(fieldName: String) = name(fieldName) // name("public", name, fieldName)

object Database {


    val manuscriptTable = table("manuscript")!!
    val manuscriptId = field(name("manId"), UUID::class.java)!!
    val payload = field("payload", String::class.java)!!

    val transactionIdSequence = sequence("transactionid_seq")

    fun createDbTables(conn: Connection) {
        val dataContext = using(conn, SQLDialect.H2)

        dataContext
            .createTableIfNotExists(TransactionLog.transactionLogTable)
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
            .createTableIfNotExists(processedEventTable)
            .column(ProcessedEvent.transactionId)
            .execute()

        dataContext.deleteFrom(processedEventTable).execute()
        dataContext.insertInto(processedEventTable, ProcessedEvent.transactionId).values(BigInteger.valueOf(-1))
            .execute()

        dataContext
            .createTableIfNotExists(manuscriptTable)
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
            .from(manuscriptTable)
            .where(manuscriptId.eq(id.raw)).fetchOne()
        return record
            ?.let {
                Manuscript.fromJson(it.value1())
            }

    }

    private fun intRangeFromDbFields(start: Int?, end: Int?) =
        if (start == null || end == null) null else IntRange(start, end)

    fun saveManuscriptToDb(dataContext: DSLContext, manuscript: Manuscript) {
        dataContext.update(Database.manuscriptTable)
            .set(Database.payload, manuscript.toJson())
            .where(Database.manuscriptId.eq(manuscript.id.raw)).execute()
    }
}


