@file:UseSerializers(LocalDateTimeSerializer::class)

package com.github.plplmax.planning.timeslots

import com.github.plplmax.planning.plugins.serialization.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.LocalDateTime

@Serializable
data class Timeslot(val id: Int = 0, val start: LocalDateTime, val end: LocalDateTime)
