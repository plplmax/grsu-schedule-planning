package com.github.plplmax.planning.rooms

import kotlinx.serialization.Serializable

@Serializable
data class Room(val id: Int, val name: String, val capacity: Int)
