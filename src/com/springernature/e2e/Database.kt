package com.springernature.e2e

import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL.*
import java.math.BigInteger
import java.sql.Connection
import java.util.*

object Database {
    val transactionLog = table("transactionLog")!!
    val transactionId = field("tranId", BigInteger::class.java)!!
    val transactionType = field("type", String::class.java)!!
    val payload = field("payload", String::class.java)!!

    val processedEvent = table("processedEvent")!!

    val manuscript = table("manuscript")!!
    val manuscriptId = field("manId", UUID::class.java)!!
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
            .createTableIfNotExists(transactionLog)
            .column(transactionId)
            .column(transactionType)
            .column(manuscriptId)
            .column(payload)
            .constraint(
                constraint("transactionlog_pk").primaryKey(transactionId)
            )
            .execute()

        dataContext.createSequenceIfNotExists(transactionIdSequence)
            .execute()

        dataContext
            .createTableIfNotExists(processedEvent)
            .column(transactionId)
            .execute()

        dataContext.insertInto(processedEvent, transactionId).values(BigInteger.valueOf(-1))
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
        val record = dataContext
            .select(
                Database.title, Database.titleApproved, Database.titleStart, Database.titleEnd,
                Database.abstract, Database.abstractApproved, Database.abstractStart, Database.abstractEnd,
                Database.content, Database.contentApproved
            )
            .from(Database.manuscript)
            .where(Database.manuscriptId.eq(id.raw)).fetchOne()
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
            ?: throw RuntimeException("not found")
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


