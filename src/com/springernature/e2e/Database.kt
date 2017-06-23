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
    val title = field("title", String::class.java)!!
    val titleApproved = field("title_approved", Boolean::class.java)!!
    val titleStart = field("title_start", Integer::class.java)!!
    val titleEnd = field("title_end", Integer::class.java)!!
    val abstract = field("abstract", String::class.java)!!
    val abstractApproved = field("abstract_approved", Boolean::class.java)!!
    val abstractStart = field("abstract_start", Integer::class.java)!!
    val abstractEnd = field("abstract_end", Integer::class.java)!!
    val content = field("content", String::class.java)!!
    val contentApproved = field("content_approved", Boolean::class.java)!!

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
            .column(title)
            .column(titleApproved)
            .column(titleStart)
            .column(titleEnd)
            .column(abstract)
            .column(abstractStart)
            .column(abstractEnd)
            .column(abstractApproved)
            .column(content)
            .column(contentApproved)
            .execute()
    }

    fun retrieveManuscript(dataContext: DSLContext, id: ManuscriptId): Manuscript {
        return maybeRetrieveManuscript(dataContext, id) ?: throw RuntimeException("not found")
    }

    fun maybeRetrieveManuscript(dataContext: DSLContext, id: ManuscriptId): Manuscript? {
        val record = dataContext
            .select(
                title, titleApproved, titleStart, titleEnd,
                abstract, abstractApproved, abstractStart, abstractEnd,
                content, contentApproved
            )
            .from(manuscript)
            .where(manuscriptId.eq(id.raw)).fetchOne()
        return record
            ?.let {
                val result = Manuscript(
                    id,
                    MarkUpFragment(MarkUp(it.value1() ?: ""), it.value2() ?: false, intRangeFromDbFields(it.value3()?.toInt(), it.value4()?.toInt())),
                    MarkUpFragment(MarkUp(it.value5() ?: ""), it.value6() ?: false, intRangeFromDbFields(it.value7()?.toInt(), it.value8()?.toInt())),
                    MarkUpFragment(MarkUp(it.value9() ?: ""), it.value10() ?: false, null)
                )
                println(">>>>>>>>>> result = $result")
                result
            }

    }

    private fun intRangeFromDbFields(start: Int?, end: Int?) =
        if (start == null || end == null) null else IntRange(start, end)

    fun saveManuscriptToDb(dataContext: DSLContext, manuscript: Manuscript) {
        dataContext.update(Database.manuscript)
            .set(Database.title, manuscript.title.markUp.raw)
            .set(Database.titleApproved, manuscript.title.approved)
            .set(Database.titleStart, manuscript.title.originalDocumentLocation?.let {
                Integer(manuscript.title.originalDocumentLocation.first)
            })
            .set(Database.titleEnd, manuscript.title.originalDocumentLocation?.let {
                Integer(manuscript.title.originalDocumentLocation.last)
            })
            .set(Database.abstract, manuscript.abstract.markUp.raw)
            .set(Database.abstractApproved, manuscript.abstract.approved)
            .set(Database.abstractStart, manuscript.abstract.originalDocumentLocation?.let {
                Integer(manuscript.abstract.originalDocumentLocation.first)
            })
            .set(Database.abstractEnd, manuscript.abstract.originalDocumentLocation?.let {
                Integer(manuscript.abstract.originalDocumentLocation.last)
            })
            .set(Database.content, manuscript.content.markUp.raw)
            .set(Database.contentApproved, manuscript.content.approved)
            .where(Database.manuscriptId.eq(manuscript.id.raw)).execute()
    }
}


