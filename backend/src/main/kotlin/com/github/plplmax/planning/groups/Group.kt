package com.github.plplmax.planning.groups

import kotlinx.serialization.Serializable

@Serializable
data class Group(val id: Int, val number: Int, val letter: Char)
