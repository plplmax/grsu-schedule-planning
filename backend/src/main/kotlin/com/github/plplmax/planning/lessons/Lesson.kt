package com.github.plplmax.planning.lessons

import ai.timefold.solver.core.api.domain.entity.PlanningEntity
import ai.timefold.solver.core.api.domain.lookup.PlanningId
import ai.timefold.solver.core.api.domain.variable.PlanningVariable
import com.github.plplmax.planning.groups.Group
import com.github.plplmax.planning.rooms.Room
import com.github.plplmax.planning.subjects.Subject
import com.github.plplmax.planning.teachers.Teacher
import com.github.plplmax.planning.timeslots.Timeslot
import kotlinx.serialization.Serializable

@Serializable
@PlanningEntity
data class Lesson(
    @PlanningId
    val id: Int = 0,
    val group: Group = Group(0, 0, ' '),
    val teacher: Teacher = Teacher(0, "", ""),
    val subject: Subject = Subject(0, "", 0, 0, false),
    val room: Room = Room(0, ""),
    @PlanningVariable
    val timeslot: Timeslot? = null
)
