package com.github.plplmax.planning.groups

import com.github.plplmax.planning.lessons.Lesson
import kotlinx.serialization.Serializable

@Serializable
data class NewGroup(val number: Int, val letter: Char, val lessons: List<Lesson>)
