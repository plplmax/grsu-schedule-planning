package com.github.plplmax.planning.subjects

import com.github.plplmax.planning.plugins.routing.AppRoute
import com.github.plplmax.planning.plugins.routing.AppRouteBasic
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class SubjectsRoute(private val subjects: Subjects, vararg children: AppRoute) : AppRouteBasic(*children) {
    override fun install(parent: Route) {
        parent.route("/subjects") {
            super.install(this)
            get {
                call.respond(subjects.all())
            }
            post {
                call.receive<NewSubject>().let { subjects.insert(it) }.let { call.respond(it) }
            }
            put("/{id}") {
                call.parameters["id"]
                    ?.toIntOrNull()
                    ?.let {
                        val subject = call.receive<NewSubject>()
                        SubjectDetail(
                            id = it,
                            name = subject.name,
                            complexity = subject.complexity,
                            disallowedTimeslots = subject.disallowedTimeslots,
                            minDaysBetween = subject.minDaysBetween,
                            minDaysStrict = subject.minDaysStrict,
                            onceFirstOrLastTimeslot = subject.onceFirstOrLastTimeslot
                        )
                    }
                    ?.let { subjects.update(it) }
                    ?.let { call.respond(it) }
                    ?: call.respond(HttpStatusCode.BadRequest, "parameter `id` is not an integer")
            }
            delete("/{id}") {
                call.parameters["id"]
                    ?.toIntOrNull()
                    ?.let { subjects.delete(it) }
                    ?.let { call.respond(it) }
                    ?: call.respond(HttpStatusCode.BadRequest, "parameter `id` is not an integer")
            }
        }
    }
}
