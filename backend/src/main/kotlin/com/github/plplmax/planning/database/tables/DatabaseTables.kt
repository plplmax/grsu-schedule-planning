package com.github.plplmax.planning.database.tables

import org.jetbrains.exposed.sql.Table

class DatabaseTables : Tables {
    override fun all(): List<Table> = listOf(
        TimeslotsTable,
        SubjectsTable,
        TeachersTable,
        TeachersToSubjectsTable,
        RoomsTable,
        GroupsTable,
        LessonsTable
    )
}
