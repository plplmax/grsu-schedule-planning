package com.github.plplmax.planning.teachers

import com.github.plplmax.planning.plugins.routing.AppRoute
import com.github.plplmax.planning.plugins.routing.AppRouteBasic
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class TeachersRoute(private val teachers: Teachers, vararg children: AppRoute) : AppRouteBasic(*children) {
    override fun install(parent: Route) {
        parent.route("/teachers") {
            super.install(this)
            get {
                call.respond(teachers.all())
            }
            get("/details") {
                call.respond(teachers.allDetails())
            }
            get("/{id}") {
                call.parameters["id"]
                    ?.toIntOrNull()
                    ?.let { teachers.findById(it) }
                    ?.let { teacher ->
                        if (teacher.isPresent) call.respond(teacher.get()) else call.respond(HttpStatusCode.NotFound)
                    }
                    ?: call.respond(HttpStatusCode.BadRequest, PARAMETER_ERROR)
            }
            post {
                call.receive<NewTeacher>().let { teachers.insert(it) }.let { call.respond(it) }
            }
            put("/{id}") {
                call.parameters["id"]
                    ?.toIntOrNull()
                    ?.let {
                        val teacher = call.receive<NewTeacher>()
                        TeacherDetail(
                            id = it,
                            firstname = teacher.firstname,
                            lastname = teacher.lastname,
                            subjects = teacher.subjects,
                            lessons = teacher.lessons
                        )
                    }
                    ?.let { teachers.update(it) }
                    ?.let { call.respond(it) }
                    ?: call.respond(HttpStatusCode.BadRequest, PARAMETER_ERROR)
            }
            delete("/{id}") {
                call.parameters["id"]
                    ?.toIntOrNull()
                    ?.let { teachers.delete(it) }
                    ?.let { call.respond(it) }
                    ?: call.respond(HttpStatusCode.BadRequest, PARAMETER_ERROR)
            }
        }
    }

    companion object {
        private const val PARAMETER_ERROR = "parameter `id` is not an integer"
    }
}
