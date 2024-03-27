package com.github.plplmax.planning.plugins.routing

import com.github.plplmax.planning.plugins.AppPlugin
import com.github.plplmax.planning.timeslots.PgTimeslots
import com.github.plplmax.planning.timeslots.TimeslotsRoute
import com.github.plplmax.planning.timeslots.ValidatedTimeslots
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database

class RoutingPlugin(private val database: Database) : AppPlugin {
    override fun install(app: Application) {
        app.routing {
            ApiRoute(
                TimeslotsRoute(ValidatedTimeslots(PgTimeslots(database)))
            ).install(this)
        }
    }
}
