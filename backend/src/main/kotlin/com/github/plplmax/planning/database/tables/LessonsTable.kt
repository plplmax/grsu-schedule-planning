package com.github.plplmax.planning.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object LessonsTable : IntIdTable() {
    val groupId = reference("groupId", GroupsTable, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val teacherId = entityId("teacherId", TeachersTable)
    val subjectId = entityId("subjectId", SubjectsTable)
    val roomId = reference("roomId", RoomsTable, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val timeslotId = optReference("timeslotId", TimeslotsTable, onDelete = ReferenceOption.SET_NULL, onUpdate = ReferenceOption.CASCADE)

    init {
        foreignKey(teacherId, subjectId, target = TeachersToSubjectsTable.primaryKey, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    }
}
