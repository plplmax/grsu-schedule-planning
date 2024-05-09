package com.github.plplmax.planning.lessons

import com.github.plplmax.planning.groups.Group
import com.github.plplmax.planning.rooms.Room
import com.github.plplmax.planning.subjects.Subject
import com.github.plplmax.planning.teachers.Teacher
import com.github.plplmax.planning.timeslots.Timeslot
import kotlinx.serialization.Serializable

@Serializable
data class Lesson(
    val id: Int,
    val group: Group,
    val teacher: Teacher,
    val subject: Subject,
    val room: Room,
    val timeslot: Timeslot?
)
