package com.github.plplmax.planning.database

import com.github.plplmax.planning.database.tables.Tables
import org.jetbrains.exposed.sql.Database

interface AppDatabase {
    fun connected(): Database
    fun create(tables: Tables)
}
