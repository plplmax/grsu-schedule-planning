package com.github.plplmax.planning.subjects.paired

import com.github.plplmax.planning.groups.Group
import com.github.plplmax.planning.subjects.Subject
import kotlinx.serialization.Serializable

@Serializable
data class PairedSubjects(
    val id: Int,
    val firstSubject: Subject,
    val secondSubject: Subject,
    val groups: List<Group>,
    val count: Int
)
