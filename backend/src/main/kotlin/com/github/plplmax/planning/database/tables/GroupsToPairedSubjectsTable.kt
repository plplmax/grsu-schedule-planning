package com.github.plplmax.planning.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object GroupsToPairedSubjectsTable : IntIdTable() {
    val groupId = reference("groupId", GroupsTable, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val pairedSubjectsId = reference("pairedSubjectsId", PairedSubjectsTable, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)

    init {
        uniqueIndex(groupId, pairedSubjectsId)
    }
}
