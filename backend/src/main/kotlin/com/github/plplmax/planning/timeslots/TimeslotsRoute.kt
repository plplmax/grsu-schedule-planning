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
                call.respond(timeslots.all())
            }
            post {
                call.receive<NewTimeslot>().let { timeslots.insert(it) }.let { call.respond(it) }
            }
            put("/{id}") {
                call.parameters["id"]
                    ?.toIntOrNull()
                    ?.let {
                        val timeslot = call.receive<NewTimeslot>()
                        Timeslot(id = it, dayOfWeek = timeslot.dayOfWeek, start = timeslot.start, end = timeslot.end)
                    }
                    ?.let { timeslots.update(it) }
                    ?.let { call.respond(it) }
                    ?: call.respond(HttpStatusCode.BadRequest, "parameter `id` is not an integer")
            }
            delete("/{id}") {
                call.parameters["id"]
                    ?.toIntOrNull()
                    ?.let { timeslots.delete(it) }
                    ?.let { call.respond(it) }
                    ?: call.respond(HttpStatusCode.BadRequest, "parameter `id` is not an integer")
            }
        }
    }
}
