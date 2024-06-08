package com.github.plplmax.planning.teachers

import kotlinx.serialization.Serializable

@Serializable
data class TeacherShort(val id: Int, val firstname: String, val lastname: String)
