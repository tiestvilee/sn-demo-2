package com.springernature.e2e

import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.jooq.impl.DSL.*
import java.sql.Connection
import java.util.*

object db {
    val manuscript = table("manuscript")
    val manuscriptId = field("id", UUID::class.java)
    val title = field("title", String::class.java)
    val abstract = field("abstract", String::class.java)

    fun createDbTables(conn: Connection) {
        val dataContext = using(conn, SQLDialect.H2)

        dataContext
            .createTableIfNotExists(manuscript)
            .column(manuscriptId)
            .column(title)
            .column(abstract)
            .execute()
    }
}

fun retrieveManuscript(dataContext: DSLContext, id: ManuscriptId): Manuscript {
    val record = dataContext.select(db.title, db.abstract).from(db.manuscript).where(db.manuscriptId.eq(id.raw)).fetchOne()
    return record
        ?.let { Manuscript(id, MarkUp(it.value1() ?: "Manuscript: ${id.raw}"), MarkUp(it.value2() ?: "")) }
        ?: throw RuntimeException("not found")
}