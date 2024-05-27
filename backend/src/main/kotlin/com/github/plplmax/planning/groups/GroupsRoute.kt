package com.github.plplmax.planning.groups

import com.github.plplmax.planning.plugins.routing.AppRoute
import com.github.plplmax.planning.plugins.routing.AppRouteBasic
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class GroupsRoute(private val groups: Groups, vararg children: AppRoute) : AppRouteBasic(*children) {
    override fun install(parent: Route) {
        parent.route("/groups") {
            super.install(this)
            get {
                call.respond(groups.all())
            }
            get("/details") {
                call.respond(groups.allDetails())
            }
            get("/{id}") {
                call.parameters["id"]
                    ?.toIntOrNull()
                    ?.let { groups.findById(it) }
                    ?.let { group -> if (group.isPresent) call.respond(group.get()) else call.respond(HttpStatusCode.NotFound) }
                    ?: call.respond(HttpStatusCode.BadRequest, PARAMETER_ERROR)
            }
            post {
                call.receive<NewGroup>().let { groups.insert(it) }.let { call.respond(it) }
            }
            put("/{id}") {
                call.parameters["id"]
                    ?.toIntOrNull()
                    ?.let {
                        val group = call.receive<NewGroup>()
                        GroupDetail(id = it, number = group.number, letter = group.letter, lessons = group.lessons)
                    }
                    ?.let { groups.update(it) }
                    ?.let { call.respond(it) }
                    ?: call.respond(HttpStatusCode.BadRequest, PARAMETER_ERROR)
            }
            delete("/{id}") {
                call.parameters["id"]
                    ?.toIntOrNull()
                    ?.let { groups.delete(it) }
                    ?.let { call.respond(it) }
                    ?: call.respond(HttpStatusCode.BadRequest, PARAMETER_ERROR)
            }
        }
    }

    companion object {
        private const val PARAMETER_ERROR = "parameter `id` is not an integer"
    }
}
