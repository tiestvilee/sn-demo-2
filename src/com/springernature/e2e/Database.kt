package com.springernature.e2e

import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL.*
import java.sql.Connection
import java.util.*

object Database {
    val manuscript = table("manuscript")!!
    val manuscriptId = field("id", UUID::class.java)!!
    val title = field("title", String::class.java)!!
    val titleApproved = field("title_approved", Boolean::class.java)!!
    val titleStart = field("title_start", Integer::class.java)!!
    val titleEnd = field("title_end", Integer::class.java)!!
    val abstract = field("abstract", String::class.java)!!
    val abstractApproved = field("abstract_approved", Boolean::class.java)!!
    val abstractStart = field("abstract_start", Integer::class.java)!!
    val abstractEnd = field("abstract_end", Integer::class.java)!!

    fun createDbTables(conn: Connection) {
        val dataContext = using(conn, SQLDialect.H2)

        dataContext
            .createTableIfNotExists(manuscript)
            .column(manuscriptId)
            .column(title)
            .column(titleApproved)
            .column(titleStart)
            .column(titleEnd)
            .column(abstract)
            .column(abstractApproved)
            .column(abstractStart)
            .column(abstractEnd)
            .execute()
    }

    fun retrieveManuscript(dataContext: DSLContext, id: ManuscriptId): Manuscript {
        val record = dataContext
            .select(
                Database.title, Database.titleApproved, Database.titleStart, Database.titleEnd,
                Database.abstract, Database.abstractApproved, Database.abstractStart, Database.abstractEnd)
            .from(Database.manuscript)
            .where(Database.manuscriptId.eq(id.raw)).fetchOne()
        return record
            ?.let {
                val result = Manuscript(
                    id,
                    MarkUpFragment(MarkUp(it.value1() ?: ""), it.value2() ?: false, intRangeFromDbFields(it.value3()?.toInt(), it.value4()?.toInt())),
                    MarkUpFragment(MarkUp(it.value5() ?: ""), it.value6() ?: false, intRangeFromDbFields(it.value7()?.toInt(), it.value8()?.toInt()))
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
            .where(Database.manuscriptId.eq(manuscript.id.raw)).execute()
    }
}


