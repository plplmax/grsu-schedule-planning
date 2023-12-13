package com.github.plplmax.planning.database.tables

import org.jetbrains.exposed.sql.Table

fun interface Tables {
    fun all(): List<Table>
}
