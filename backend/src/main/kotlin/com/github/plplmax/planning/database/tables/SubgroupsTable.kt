package com.github.plplmax.planning.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object SubgroupsTable : IntIdTable() {
    val name = varchar("name", 64)
    val divisionId = reference("divisionId", DivisionsTable, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)

    init {
        uniqueIndex(name, divisionId)
    }
}
