@file:UseSerializers(LocalTimeSerializer::class, DayOfWeekSerializer::class)

package com.github.plplmax.planning.timeslots

import ai.timefold.solver.core.api.domain.lookup.PlanningId
import com.github.plplmax.planning.plugins.serialization.DayOfWeekSerializer
import com.github.plplmax.planning.plugins.serialization.LocalTimeSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.DayOfWeek
import java.time.LocalTime

@Serializable
data class Timeslot(@PlanningId val id: Int, val dayOfWeek: DayOfWeek, val start: LocalTime, val end: LocalTime)
