package com.github.plplmax.planning.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object SubjectsTable : IntIdTable() {
    val name = varchar("name", 64).uniqueIndex()
    val complexity = integer("complexity").default(0)
    val minDaysBetween = integer("minDaysBetween").default(1)
    val minDaysStrict = bool("minDaysStrict").default(false)
    val onceFirstOrLastTimeslot = bool("onceFirstOrLastTimeslot").default(false)
}
