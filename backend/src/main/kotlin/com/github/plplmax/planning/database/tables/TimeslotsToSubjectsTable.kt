package com.github.plplmax.planning.database.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object TimeslotsToSubjectsTable : Table() {
    val timeslotId = reference("timeslotId", TimeslotsTable, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val subjectId = reference("subjectId", SubjectsTable, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(timeslotId, subjectId)
}
