package com.springernature.e2e

import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.jooq.impl.DSL.*
import java.sql.Connection
import java.util.*

object db {
    val manuscript = table("manuscript")
    val manuscriptId = field("id", UUID::class.java)
    val title = field("title", String::class.java)

    fun createDbTables(conn: Connection) {
        val dataContext = using(conn, SQLDialect.H2)

        dataContext
            .createTableIfNotExists(manuscript)
            .column(manuscriptId)
            .column(title)
            .execute()
    }
}