package com.github.plplmax.planning

import com.github.plplmax.planning.database.PgDatabase
import com.github.plplmax.planning.database.tables.DatabaseTables
import com.github.plplmax.planning.plugins.routing.RoutingPlugin
import com.github.plplmax.planning.plugins.serialization.SerializationPlugin
import io.ktor.server.application.*
import io.ktor.server.netty.*

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    val pg = PgDatabase(
        config = environment.config.config("database.postgres"),
        development = environment.config.propertyOrNull("ktor.development")?.getString().toBoolean()
    ).also { it.create(DatabaseTables()) }
    SerializationPlugin().install(this)
    RoutingPlugin(pg.connected()).install(this)
}
