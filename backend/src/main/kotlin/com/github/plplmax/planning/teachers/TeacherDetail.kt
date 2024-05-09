package com.github.plplmax.planning.teachers

import com.github.plplmax.planning.subjects.Subject
import kotlinx.serialization.Serializable

@Serializable
data class TeacherDetail(val id: Int, val firstname: String, val lastname: String, val subjects: List<Subject>)
