package com.github.plplmax.planning.lessons

import ai.timefold.solver.core.api.domain.entity.PlanningEntity
import ai.timefold.solver.core.api.domain.lookup.PlanningId
import ai.timefold.solver.core.api.domain.variable.PlanningVariable
import com.github.plplmax.planning.divisions.Division
import com.github.plplmax.planning.divisions.SubgroupDetail
import com.github.plplmax.planning.groups.Group
import com.github.plplmax.planning.rooms.Room
import com.github.plplmax.planning.subjects.Subject
import com.github.plplmax.planning.teachers.TeacherShort
import com.github.plplmax.planning.timeslots.Timeslot
import kotlinx.serialization.Serializable

@Serializable
@PlanningEntity
data class Lesson(
    @PlanningId
    val id: Int = 0,
    val group: Group = Group(0, 0, ' '),
    val subgroup: SubgroupDetail = SubgroupDetail(0, "", Division(0)),
    val teacher: TeacherShort = TeacherShort(0, "", ""),
    val subject: Subject = Subject(0, "", 0, 0, minDaysStrict = false, onceFirstOrLastTimeslot = false),
    val room: Room = Room(0, "", 0),
    @PlanningVariable
    val timeslot: Timeslot? = null
)
