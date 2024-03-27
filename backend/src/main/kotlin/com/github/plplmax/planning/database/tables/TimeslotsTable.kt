package com.github.plplmax.planning.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.javatime.time
import java.time.DayOfWeek
import java.time.LocalTime

object TimeslotsTable : IntIdTable() {
    val dayOfWeek: Column<DayOfWeek> = enumeration("dayOfWeek")
    val start: Column<LocalTime> = time("start")
    val end: Column<LocalTime> = time("end")

    init {
        uniqueIndex(dayOfWeek, start, end)
    }
}
