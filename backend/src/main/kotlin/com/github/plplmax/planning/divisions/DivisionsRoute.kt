package com.github.plplmax.planning.divisions

import com.github.plplmax.planning.plugins.routing.AppRoute
import com.github.plplmax.planning.plugins.routing.AppRouteBasic
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class DivisionsRoute(private val divisions: Divisions, vararg children: AppRoute) : AppRouteBasic(*children) {
    override fun install(parent: Route) {
        parent.route("/divisions") {
            super.install(this)
            get {
                call.respond(divisions.all())
            }
            get("/subgroups") {
                call.respond(divisions.allSubgroups())
            }
            post {
                call.receive<NewDivision>().let { divisions.insert(it) }.let { call.respond(it) }
            }
            put("/{id}") {
                call.parameters["id"]
                    ?.toIntOrNull()
                    ?.let {
                        val division = call.receive<NewDivision>()
                        DivisionDetail(id = it, subgroups = division.subgroups)
                    }
                    ?.let { divisions.update(it) }
                    ?.let { call.respond(it) }
                    ?: call.respond(HttpStatusCode.BadRequest, PARAMETER_ERROR)
            }
            delete("/{id}") {
                call.parameters["id"]
                    ?.toIntOrNull()
                    ?.let { divisions.delete(it) }
                    ?.let { call.respond(it) }
                    ?: call.respond(HttpStatusCode.BadRequest, PARAMETER_ERROR)
            }
        }
    }

    companion object {
        private const val PARAMETER_ERROR = "parameter `id` is not an integer"
    }
}
