package com.github.plplmax.planning.timeslots

import com.github.plplmax.planning.plugins.routing.AppRoute
import com.github.plplmax.planning.plugins.routing.AppRouteBasic
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class TimeslotsRoute(private val timeslots: Timeslots, vararg children: AppRoute) : AppRouteBasic(*children) {
    override fun install(parent: Route) {
        parent.route("/timeslots") {
            super.install(this)
            get {
                call.respond(timeslots.all().groupBy { it.start.toLocalDate().toString() })
            }
            post {
                call.receive<TimeslotsRangeOf>().save(timeslots)
                call.respond(HttpStatusCode.NoContent)
            }
            delete("/{id}") {
                call.parameters["id"]
                    ?.toIntOrNull()
                    ?.let { timeslots.delete(listOf(it)) }
                    ?: call.respond(HttpStatusCode.BadRequest, "id parameter is not an integer")
                call.respond(HttpStatusCode.NoContent)
            }
            delete {
                call.request.queryParameters.getAll("ids")
                    ?.map(String::toInt)
                    ?.let { timeslots.delete(it) }
                    ?: call.respond(HttpStatusCode.BadRequest, "There is no ids query parameter")
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
