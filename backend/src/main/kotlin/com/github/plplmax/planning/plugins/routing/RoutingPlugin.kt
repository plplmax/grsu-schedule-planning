package com.github.plplmax.planning.plugins.routing

import com.github.plplmax.planning.groups.GroupsRoute
import com.github.plplmax.planning.groups.PgGroups
import com.github.plplmax.planning.groups.ValidatedGroups
import com.github.plplmax.planning.plugins.AppPlugin
import com.github.plplmax.planning.rooms.PgRooms
import com.github.plplmax.planning.rooms.RoomsRoute
import com.github.plplmax.planning.rooms.ValidatedRooms
import com.github.plplmax.planning.subjects.PgSubjects
import com.github.plplmax.planning.subjects.SubjectsRoute
import com.github.plplmax.planning.subjects.ValidatedSubjects
import com.github.plplmax.planning.teachers.PgTeachers
import com.github.plplmax.planning.teachers.TeachersRoute
import com.github.plplmax.planning.teachers.ValidatedTeachers
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
                TimeslotsRoute(ValidatedTimeslots(PgTimeslots(database))),
                SubjectsRoute(ValidatedSubjects(PgSubjects(database))),
                TeachersRoute(ValidatedTeachers(PgTeachers(database))),
                RoomsRoute(ValidatedRooms(PgRooms(database))),
                GroupsRoute(ValidatedGroups(PgGroups(database)))
            ).install(this)
        }
    }
}
