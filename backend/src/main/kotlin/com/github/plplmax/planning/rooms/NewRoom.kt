package com.github.plplmax.planning.rooms

import kotlinx.serialization.Serializable

@Serializable
data class NewRoom(val name: String, val capacity: Int)
