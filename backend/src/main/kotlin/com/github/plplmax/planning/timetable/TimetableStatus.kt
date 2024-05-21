package com.github.plplmax.planning.timetable

import ai.timefold.solver.core.api.solver.SolverStatus
import kotlinx.serialization.Serializable

@Serializable
data class TimetableStatus(val status: SolverStatus)
