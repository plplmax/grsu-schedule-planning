package com.github.plplmax.planning.subjects

import kotlinx.serialization.Serializable

@Serializable
data class Subject(
    val id: Int,
    val name: String,
    val complexity: Int,
    val minDaysBetween: Int,
    val minDaysStrict: Boolean,
    val onceFirstOrLastTimeslot: Boolean
)
