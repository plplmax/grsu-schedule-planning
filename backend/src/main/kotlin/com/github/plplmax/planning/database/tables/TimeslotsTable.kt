package com.github.plplmax.planning.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object TimeslotsTable : IntIdTable() {
    val start: Column<LocalDateTime> = datetime("start")
    val end: Column<LocalDateTime> = datetime("end")

    init {
        uniqueIndex(start, end)
    }
}
