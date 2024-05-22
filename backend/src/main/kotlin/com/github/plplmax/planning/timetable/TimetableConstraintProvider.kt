package com.github.plplmax.planning.timetable

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore
import ai.timefold.solver.core.api.score.stream.Constraint
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors.*
import ai.timefold.solver.core.api.score.stream.ConstraintFactory
import ai.timefold.solver.core.api.score.stream.ConstraintProvider
import ai.timefold.solver.core.api.score.stream.Joiners
import com.github.plplmax.planning.lessons.Lesson
import com.github.plplmax.planning.timeslots.Timeslot
import java.util.function.Function
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class TimetableConstraintProvider : ConstraintProvider {
    override fun defineConstraints(constraintFactory: ConstraintFactory): Array<Constraint>? {
        return arrayOf(
            roomConflict(constraintFactory),
            teacherConflict(constraintFactory),
            groupConflict(constraintFactory),
            evenlyDistributedLessonsPerDay(constraintFactory),
            noGapsAtFirstTimeslotEachDay(constraintFactory),
            subjectVarietyPerDay(constraintFactory)
        )
    }

    private fun roomConflict(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory
            .forEachUniquePair(
                Lesson::class.java,
                Joiners.equal(Lesson::timeslot),
                Joiners.equal(Lesson::room)
            )
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Room conflict");
    }

    private fun teacherConflict(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory
            .forEachUniquePair(
                Lesson::class.java,
                Joiners.equal(Lesson::timeslot),
                Joiners.equal(Lesson::teacher)
            )
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Teacher conflict");
    }

    private fun groupConflict(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory
            .forEachUniquePair(
                Lesson::class.java,
                Joiners.equal(Lesson::timeslot),
                Joiners.equal(Lesson::group)
            )
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Group conflict");
    }

    private fun evenlyDistributedLessonsPerDay(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory.forEach(Lesson::class.java)
            .groupBy({ it.timeslot?.dayOfWeek }, Lesson::group, count())
            .concat(
                constraintFactory.forEach(Timeslot::class.java)
                    .groupBy(Timeslot::dayOfWeek)
                    .join(
                        constraintFactory.forEach(Lesson::class.java)
                            .groupBy(Lesson::group)
                    ).ifNotExists(
                        Lesson::class.java,
                        Joiners.equal(
                            { dayOfWeek, group -> dayOfWeek to group },
                            { lesson -> lesson.timeslot?.dayOfWeek to lesson.group })
                    )
            ).flattenLast { listOf(it ?: 0) }
            .groupBy({ _, group, _ -> group }, toMap({ dayOfWeek, _, _ -> dayOfWeek }, { _, _, count -> count }))
            .map({ group, _ -> group }, { _, dayOfWeeks -> dayOfWeeks.mapValues { entry -> entry.value.first() } })
            .penalize(HardSoftScore.ONE_SOFT) { _, dayOfWeeks ->
                with(dayOfWeeks) { -values.sumOf { it * it } + (values.sumOf { it } * values.sumOf { it } * 1.0 / size).roundToInt() }.absoluteValue
            }
            .asConstraint("Evenly distributed lessons per day")
    }

    private fun noGapsAtFirstTimeslotEachDay(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory.forEach(Timeslot::class.java)
            .groupBy(Timeslot::dayOfWeek, toList())
            .map { _, timeslots -> timeslots.first() }
            .join(Lesson::class.java, Joiners.equal(Function.identity(), Lesson::timeslot))
            .groupBy(
                { timeslot, _ -> timeslot },
                { _, lesson -> lesson.group },
                collectAndThen(toList { _: Timeslot, lesson: Lesson -> lesson }) { it.first() }
            ).concat(
                constraintFactory.forEach(Timeslot::class.java)
                    .groupBy(Timeslot::dayOfWeek, toList())
                    .map { _, timeslots -> timeslots.first() }
                    .join(
                        constraintFactory.forEach(Lesson::class.java)
                            .groupBy(Lesson::group)
                    ).ifNotExists(
                        Lesson::class.java,
                        Joiners.equal(
                            { timeslot, group -> timeslot to group },
                            { lesson -> lesson.timeslot to lesson.group })
                    )
            )
            .filter { _, _, lesson -> lesson == null }
            .ifExists(
                Lesson::class.java,
                Joiners.equal(
                    { timeslot, group, _ -> timeslot.dayOfWeek to group },
                    { lesson -> lesson.timeslot?.dayOfWeek to lesson.group })
            )
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("No gaps at first timeslot each day")
    }

    private fun subjectVarietyPerDay(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory.forEach(Lesson::class.java)
            .groupBy(Lesson::group, { it.timeslot?.dayOfWeek }, toList(Lesson::subject))
            .penalize(HardSoftScore.ONE_SOFT) { _, _, subjects -> subjects.size - subjects.distinct().size }
            .asConstraint("Subject variety per day")
    }
}