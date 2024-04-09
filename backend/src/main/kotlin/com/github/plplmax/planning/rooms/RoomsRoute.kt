package com.github.plplmax.planning.rooms

import com.github.plplmax.planning.plugins.routing.AppRoute
import com.github.plplmax.planning.plugins.routing.AppRouteBasic
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class RoomsRoute(private val rooms: Rooms, vararg children: AppRoute) : AppRouteBasic(*children) {
    override fun install(parent: Route) {
        parent.route("/rooms") {
            super.install(this)
            get {
                call.respond(rooms.all())
            }
            post {
                call.receive<NewRoom>().let { rooms.insert(it) }.let { call.respond(it) }
            }
            put("/{id}") {
                call.parameters["id"]
                    ?.toIntOrNull()
                    ?.let {
                        val subject = call.receive<NewRoom>()
                        Room(id = it, name = subject.name)
                    }
                    ?.let { rooms.update(it) }
                    ?.let { call.respond(it) }
                    ?: call.respond(HttpStatusCode.BadRequest, "parameter `id` is not an integer")
            }
            delete("/{id}") {
                call.parameters["id"]
                    ?.toIntOrNull()
                    ?.let { rooms.delete(it) }
                    ?.let { call.respond(it) }
                    ?: call.respond(HttpStatusCode.BadRequest, "parameter `id` is not an integer")
            }
        }
    }
}
