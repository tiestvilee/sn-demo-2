package com.springernature.e2e

import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL.*
import java.sql.Connection
import java.util.*

object Database {
    val manuscript = table("manuscript")
    val manuscriptId = field("id", UUID::class.java)
    val title = field("title", String::class.java)
    val titleApproved = field("title_approved", Boolean::class.java)
    val abstract = field("abstract", String::class.java)
    val abstractApproved = field("abstract_approved", Boolean::class.java)

    fun createDbTables(conn: Connection) {
        val dataContext = using(conn, SQLDialect.H2)

        dataContext
            .createTableIfNotExists(manuscript)
            .column(manuscriptId)
            .column(title)
            .column(titleApproved)
            .column(abstract)
            .column(abstractApproved)
            .execute()
    }

    fun retrieveManuscript(dataContext: DSLContext, id: ManuscriptId): Manuscript {
        val record = dataContext
            .select(Database.title, Database.titleApproved, Database.abstract, Database.abstractApproved)
            .from(Database.manuscript)
            .where(Database.manuscriptId.eq(id.raw)).fetchOne()
        return record
            ?.let {
                val result = Manuscript(
                    id,
                    MarkUpFragment(MarkUp(it.value1() ?: ""), it.value2() ?: false, null),
                    MarkUpFragment(MarkUp(it.value3() ?: ""), it.value4() ?: false, null)
                )
                println(">>>>>>>>>> result = ${result}")
                result
            }
            ?: throw RuntimeException("not found")
    }

    fun saveManuscriptToDb(dataContext: DSLContext, manuscript: Manuscript) {
        dataContext.update(Database.manuscript)
            .set(Database.title, manuscript.title.markUp.raw)
            .set(Database.titleApproved, manuscript.title.approved)
            .set(Database.abstract, manuscript.abstract.markUp.raw)
            .set(Database.abstractApproved, manuscript.abstract.approved)
            .where(Database.manuscriptId.eq(manuscript.id.raw)).execute()
    }
}


