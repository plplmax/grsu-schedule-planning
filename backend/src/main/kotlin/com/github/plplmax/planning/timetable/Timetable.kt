package com.github.plplmax.planning.timetable

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty
import ai.timefold.solver.core.api.domain.solution.PlanningScore
import ai.timefold.solver.core.api.domain.solution.PlanningSolution
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore
import com.github.plplmax.planning.lessons.Lesson
import com.github.plplmax.planning.subjects.SubjectDetail
import com.github.plplmax.planning.subjects.paired.PairedSubjects
import com.github.plplmax.planning.timeslots.Timeslot

@PlanningSolution
data class Timetable(
    @ProblemFactCollectionProperty
    @ValueRangeProvider
    val timeslots: List<Timeslot> = listOf(),
    @PlanningEntityCollectionProperty
    val lessons: List<Lesson> = listOf(),
    @ProblemFactCollectionProperty
    val pairedSubjects: List<PairedSubjects> = listOf(),
    @ProblemFactCollectionProperty
    val subjects: List<SubjectDetail> = listOf(),
    @PlanningScore
    val score: HardSoftScore = HardSoftScore.ZERO
)
