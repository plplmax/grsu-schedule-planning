package com.github.plplmax.planning.subjects

import com.github.plplmax.planning.timeslots.Timeslot
import kotlinx.serialization.Serializable

@Serializable
data class NewSubject(
    val name: String,
    val complexity: Int,
    val disallowedTimeslots: List<Timeslot>,
    val minDaysBetween: Int,
    val minDaysStrict: Boolean
)
