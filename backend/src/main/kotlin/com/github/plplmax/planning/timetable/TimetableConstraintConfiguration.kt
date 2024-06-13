package com.github.plplmax.planning.timetable

import ai.timefold.solver.core.api.domain.constraintweight.ConstraintConfiguration
import ai.timefold.solver.core.api.domain.constraintweight.ConstraintWeight
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore

@ConstraintConfiguration
data class TimetableConstraintConfiguration(
    @ConstraintWeight("Teacher conflict")
    private val teacherConflict: HardSoftScore = HardSoftScore.ONE_HARD,
    @ConstraintWeight("Subgroup conflict")
    private val subgroupConflict: HardSoftScore = HardSoftScore.ONE_HARD,
    @ConstraintWeight("Division conflict")
    private val divisionConflict: HardSoftScore = HardSoftScore.ONE_HARD,
    @ConstraintWeight("Room capacity")
    private val roomCapacity: HardSoftScore = HardSoftScore.ONE_HARD,
    @ConstraintWeight("No gaps at first timeslot each day")
    private val noGapsAtFirstTimeslotEachDay: HardSoftScore = HardSoftScore.ONE_HARD,
    @ConstraintWeight("Group time efficiency")
    private val groupTimeEfficiency: HardSoftScore = HardSoftScore.ONE_HARD,
    @ConstraintWeight("Subgroup time efficiency")
    private val subgroupTimeEfficiency: HardSoftScore = HardSoftScore.ONE_HARD,
    @ConstraintWeight("Disallowed subjects timeslots")
    private val disallowedSubjectsTimeslots: HardSoftScore = HardSoftScore.ONE_HARD,
    @ConstraintWeight("Paired subjects")
    private val pairedSubjects: HardSoftScore = HardSoftScore.ONE_HARD,
    @ConstraintWeight("Min days between lessons [hard]")
    private val minDaysBetweenLessonsHard: HardSoftScore = HardSoftScore.ONE_HARD,
    @ConstraintWeight("Once first or last timeslot")
    private val onceFirstOrLastTimeslot: HardSoftScore = HardSoftScore.ONE_HARD,
    @ConstraintWeight("Evenly distributed lessons per day")
    private val evenlyDistributedLessonsPerDay: HardSoftScore = HardSoftScore.ONE_HARD,
    @ConstraintWeight("Subject variety per day")
    private val subjectVarietyPerDay: HardSoftScore = HardSoftScore.ONE_HARD,
    @ConstraintWeight("Day complexity")
    private val dayComplexity: HardSoftScore = HardSoftScore.ONE_SOFT,
    @ConstraintWeight("Min days between lessons [soft]")
    private val minDaysBetweenLessonsSoft: HardSoftScore = HardSoftScore.ONE_SOFT,
    @ConstraintWeight("Teacher time efficiency")
    private val teacherTimeEfficiency: HardSoftScore = HardSoftScore.ONE_SOFT,
    @ConstraintWeight("Subject sequential variety")
    private val subjectSequentialVariety: HardSoftScore = HardSoftScore.ONE_SOFT
)