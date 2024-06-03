package com.github.plplmax.planning.timetable

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore
import ai.timefold.solver.core.api.score.stream.Constraint
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors.*
import ai.timefold.solver.core.api.score.stream.ConstraintFactory
import ai.timefold.solver.core.api.score.stream.ConstraintProvider
import ai.timefold.solver.core.api.score.stream.Joiners
import com.github.plplmax.planning.lessons.Lesson
import com.github.plplmax.planning.subjects.SubjectDetail
import com.github.plplmax.planning.subjects.paired.PairedSubjects
import com.github.plplmax.planning.timeslots.Timeslot
import java.time.DayOfWeek
import java.time.Duration
import java.util.function.Function
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class TimetableConstraintProvider : ConstraintProvider {
    override fun defineConstraints(constraintFactory: ConstraintFactory): Array<Constraint>? {
        return arrayOf(
            roomCapacity(constraintFactory),
            teacherConflict(constraintFactory),
            groupConflict(constraintFactory),
            evenlyDistributedLessonsPerDay(constraintFactory),
            noGapsAtFirstTimeslotEachDay(constraintFactory),
            subjectVarietyPerDay(constraintFactory),
            groupTimeEfficiency(constraintFactory),
            dayComplexity(constraintFactory),
            pairedSubjects(constraintFactory),
            disallowedSubjectsTimeslots(constraintFactory),
            minDaysBetweenLessonsHard(constraintFactory),
            minDaysBetweenLessonsSoft(constraintFactory),
            onceFirstOrLastTimeslot(constraintFactory),
            subgroupTimeEfficiency(constraintFactory)
        )
    }

    private fun roomCapacity(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory.forEach(Lesson::class.java)
            .groupBy(Lesson::room, Lesson::timeslot, count())
            .filter { room, _, count -> count > room.capacity }
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Room capacity")
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
                Joiners.equal(Lesson::group),
                Joiners.filtering { first, second -> first.subgroup.division != second.subgroup.division || first.subgroup == second.subgroup }
            )
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Group conflict");
    }

    private fun evenlyDistributedLessonsPerDay(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory.forEach(Lesson::class.java)
            .groupBy({ it.timeslot?.dayOfWeek }, Lesson::group, countDistinct(Lesson::timeslot))
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
                with(dayOfWeeks) {
                    -values.sumOf { it * it } + values.sumOf { it } * values.sumOf { it } * 1.0 / size
                }.let { if (it < 0) it + 1 else it }.roundToInt().absoluteValue
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
            .groupBy(Lesson::group, { it.timeslot?.dayOfWeek }, toList())
            .penalize(HardSoftScore.ONE_HARD) { _, _, lessons -> lessons.size - lessons.distinctBy { it.subject to it.subgroup }.size }
            .asConstraint("Subject variety per day")
    }

    private fun groupTimeEfficiency(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory.forEach(Lesson::class.java)
            .groupBy(
                Lesson::group,
                { it.timeslot?.dayOfWeek },
                collectAndThen(toList(Lesson::timeslot)) { timeslots -> timeslots.sortedBy { it?.start }.zipWithNext() }
            )
            .penalize(HardSoftScore.ONE_HARD) { _, _, timeslots ->
                timeslots.map {
                    val between = Duration.between(
                        it.first?.end,
                        it.second?.start
                    )
                    between >= Duration.ofMinutes(30)
                }.map { if (it) 1 else 0 }.sum()
            }
            .asConstraint("Group time efficiency")
    }

    private fun dayComplexity(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory.forEach(Lesson::class.java)
            .groupBy(
                Lesson::group,
                { it.timeslot?.dayOfWeek },
                collectAndThen(toList()) { lessons -> lessons.distinctBy { it.timeslot to it.subject.complexity } }
            )
            .concat(
                constraintFactory.forEach(Lesson::class.java)
                    .groupBy(Lesson::group)
                    .join(
                        constraintFactory.forEach(Timeslot::class.java)
                            .groupBy(Timeslot::dayOfWeek)
                    ).ifNotExists(
                        Lesson::class.java,
                        Joiners.equal(
                            { group, dayOfWeek -> group to dayOfWeek },
                            { lesson -> lesson.group to lesson.timeslot?.dayOfWeek })
                    ).expand { _, _ -> listOf() }
            ).groupBy({ group, _, _ -> group }, toMap({ _, dayOfWeek, _ -> dayOfWeek }, { _, _, lessons ->
                val timeslots = lessons.groupBy(Lesson::timeslot)
                    .mapValues { entry -> entry.value.sortedBy { lesson -> lesson.subgroup.id } }
                    .ifEmpty { return@toMap listOf(0) }
                val maxSubgroupsInTimeslot = timeslots.values.maxOf(List<Lesson>::size)
                (0 until maxSubgroupsInTimeslot).map {
                    timeslots.map { entry -> entry.value.getOrNull(it) ?: entry.value.first() }
                        .sumOf { lesson -> lesson.subject.complexity }
                }
            }))
            .map({ group, _ -> group }, { _, daysOfWeek -> daysOfWeek.mapValues { it.value.first() } })
            .penalize(HardSoftScore.ONE_SOFT) { _, daysOfWeek ->
                val subgroupsCount = daysOfWeek.values.maxOf(List<Int>::size)
                (0 until subgroupsCount).map { index ->
                    val squaredSum = daysOfWeek.values.sumOf {
                        val complexity = it.getOrNull(index) ?: it.first()
                        complexity * complexity
                    }
                    val sum = daysOfWeek.values.sumOf { it.getOrNull(index) ?: it.first() }
                    -squaredSum + sum * sum * 1.0 / daysOfWeek.size
                }.sumOf { (if (it < 0) it + 1 else it).roundToInt().absoluteValue }
            }.asConstraint("Day complexity")
    }

    private fun pairedSubjects(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory.forEach(PairedSubjects::class.java)
            .join(
                constraintFactory.forEach(PairedSubjects::class.java)
                    .flattenLast(PairedSubjects::groups)
                    .distinct(),
                Joiners.filtering { subjects, group -> subjects.groups.contains(group) }
            )
            .join(
                Lesson::class.java,
                Joiners.equal({ _, group -> group }, { lesson -> lesson.group }),
                Joiners.equal({ subjects, _ -> subjects.firstSubject }, { lesson -> lesson.subject })
            )
            .groupBy({ subjects, _, _ -> subjects }, { _, group, _ -> group }, toList { _, _, lesson -> lesson })
            .concat(
                constraintFactory.forEach(PairedSubjects::class.java)
                    .join(
                        Lesson::class.java,
                        Joiners.equal(PairedSubjects::secondSubject, Lesson::subject),
                        Joiners.filtering { subjects, lesson -> subjects.groups.contains(lesson.group) })
                    .groupBy({ subjects, _ -> subjects }, { _, lesson -> lesson.group }, toList { _, lesson -> lesson })
            )
            .groupBy({ _, group, _ -> group }, { subjects, _, _ -> subjects }, toList { _, _, lessons -> lessons })
            .map({ group, _, _ -> group }, { _, subjects, _ -> subjects }, { _, _, lessons -> lessons.flatten() })
            .penalize(HardSoftScore.ONE_HARD) { _, subjects, lessons ->
                val firstLessons = lessons.filter { it.subject == subjects.firstSubject }.sortedWith(
                    compareBy({ it.timeslot!!.dayOfWeek }, { it.timeslot!!.start })
                )
                val secondLessons = lessons.filter { it.subject == subjects.secondSubject }.sortedWith(
                    compareBy({ it.timeslot!!.dayOfWeek }, { it.timeslot!!.start })
                )

                firstLessons.zip(secondLessons).take(subjects.count).filter { (lesson1, lesson2) ->
                    val between = Duration.between(
                        lesson1.timeslot?.end,
                        lesson2.timeslot?.start
                    )
                    lesson1.timeslot?.dayOfWeek == lesson2.timeslot?.dayOfWeek && !between.isNegative
                            && between <= Duration.ofMinutes(30)
                }.let { it.size - subjects.count }.absoluteValue
            }.asConstraint("Paired subjects")
    }

    private fun disallowedSubjectsTimeslots(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory.forEach(Lesson::class.java)
            .join(SubjectDetail::class.java, Joiners.equal({ lesson -> lesson.subject.id }, SubjectDetail::id))
            .filter { lesson, subject -> subject.disallowedTimeslots.contains(lesson.timeslot) }
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Disallowed subjects timeslots")
    }

    private fun minDaysBetweenLessonsHard(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory.forEach(Lesson::class.java)
            .filter { it.subject.minDaysStrict }
            .groupBy(Lesson::group, Lesson::subject, Lesson::subgroup, toList { lesson -> lesson.timeslot!!.dayOfWeek })
            .penalize(HardSoftScore.ONE_HARD) { _, subject, _, dayOfWeeks ->
                dayOfWeeks.sortedBy(DayOfWeek::getValue)
                    .zipWithNext()
                    .sumOf { (first, second) ->
                        val diff = second.value - first.value
                        if (diff >= subject.minDaysBetween) 0 else subject.minDaysBetween - diff
                    }
            }.asConstraint("Min days between lessons [hard]")
    }

    private fun minDaysBetweenLessonsSoft(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory.forEach(Lesson::class.java)
            .filter { !it.subject.minDaysStrict }
            .groupBy(Lesson::group, Lesson::subject, Lesson::subgroup, toList { lesson -> lesson.timeslot!!.dayOfWeek })
            .impact(HardSoftScore.ONE_SOFT) { _, subject, _, dayOfWeeks ->
                dayOfWeeks.sortedBy(DayOfWeek::getValue)
                    .zipWithNext()
                    .sumOf { (first, second) ->
                        val diff = second.value - first.value
                        if (diff >= subject.minDaysBetween) diff - subject.minDaysBetween else -(subject.minDaysBetween - diff)
                    }
            }.asConstraint("Min days between lessons [soft]")
    }

    private fun onceFirstOrLastTimeslot(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory.forEach(Lesson::class.java)
            .groupBy(
                Lesson::group,
                { lesson -> lesson.timeslot!!.dayOfWeek },
                collectAndThen(toList { lesson -> lesson.timeslot!! }) { timeslots ->
                    timeslots.minBy(Timeslot::start) to timeslots.maxBy(Timeslot::start)
                })
            .groupBy({ group, _, _ -> group }, toList { _, _, timeslots -> timeslots })
            .map({ group, _ -> group }, { _, timeslots -> timeslots.flatMap { it.toList() } })
            .join(SubjectDetail::class.java, Joiners.filtering { _, _, subject -> subject.onceFirstOrLastTimeslot })
            .join(
                Lesson::class.java,
                Joiners.equal({ group, _, _ -> group }, Lesson::group),
                Joiners.equal({ _, _, subject -> subject.id }, { lesson -> lesson.subject.id })
            )
            .groupBy(
                { group, _, _, _ -> group },
                { _, timeslots, _, _ -> timeslots },
                { _, _, subject, _ -> subject },
                toList { _, _, _, lesson -> lesson.timeslot!! })
            .filter { _, firstOrLastTimeslots, _, timeslots -> !timeslots.any { firstOrLastTimeslots.contains(it) } }
            .penalize(HardSoftScore.ONE_HARD)
            .asConstraint("Once first or last timeslot")
    }

    private fun subgroupTimeEfficiency(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory.forEach(Lesson::class.java)
            .groupBy(
                Lesson::group,
                { it.subgroup.division },
                collectAndThen(toList()) { lessons -> lessons.groupBy(Lesson::subgroup) })
            .penalize(HardSoftScore.ONE_HARD) { _, _, subgroups ->
                val expectedLessonPairs = subgroups.values.maxOf(List<Lesson>::size)
                val actualLessonPairs = subgroups.values.flatten().distinctBy(Lesson::timeslot).size
                (expectedLessonPairs - actualLessonPairs).absoluteValue
            }.asConstraint("Subgroup time efficiency")
    }
}