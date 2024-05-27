package com.github.plplmax.planning.subjects.paired

import com.github.plplmax.planning.plugins.routing.AppRoute
import com.github.plplmax.planning.plugins.routing.AppRouteBasic
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class PairedSubjectsRoute(
    private val subjects: PairedSubjectsCollection,
    vararg children: AppRoute
) : AppRouteBasic(*children) {
    override fun install(parent: Route) {
        parent.route("/paired") {
            super.install(this)
            get {
                call.respond(subjects.all())
            }
            post {
                call.receive<NewPairedSubjects>().let { subjects.insert(it) }.let { call.respond(it) }
            }
            put("/{id}") {
                call.parameters["id"]
                    ?.toIntOrNull()
                    ?.let {
                        val subject = call.receive<NewPairedSubjects>()
                        PairedSubjects(
                            id = it,
                            firstSubject = subject.firstSubject,
                            secondSubject = subject.secondSubject,
                            groups = subject.groups,
                            count = subject.count
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
