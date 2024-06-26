package com.github.plplmax.planning.timetable

import ai.timefold.solver.core.api.score.stream.*
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors.*
import ai.timefold.solver.core.impl.util.Quadruple
import com.github.plplmax.planning.lessons.Lesson
import com.github.plplmax.planning.subjects.SubjectDetail
import com.github.plplmax.planning.subjects.paired.PairedSubjects
import com.github.plplmax.planning.timeslots.Timeslot
import java.time.DayOfWeek
import java.time.Duration
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class TimetableConstraintProvider : ConstraintProvider {
    override fun defineConstraints(constraintFactory: ConstraintFactory): Array<Constraint> {
        return arrayOf(
            roomCapacity(constraintFactory),
            teacherConflict(constraintFactory),
            subgroupConflict(constraintFactory),
            divisionConflict(constraintFactory),
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
            subgroupTimeEfficiency(constraintFactory),
            subjectSequentialVariety(constraintFactory),
            teacherTimeEfficiency(constraintFactory)
        )
    }

    internal fun roomCapacity(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory.forEach(Lesson::class.java)
            .groupBy(Lesson::room, Lesson::timeslot, count(), toList())
            .filter { room, _, count, _ -> count > room.capacity }
            .penalizeConfigurable { room, _, count, _ -> count - room.capacity }
            .asConstraint("Room capacity")
    }

    internal fun teacherConflict(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory
            .forEachUniquePair(
                Lesson::class.java,
                Joiners.equal(Lesson::timeslot),
                Joiners.equal(Lesson::teacher)
            )
            .penalizeConfigurable()
            .asConstraint("Teacher conflict")
    }

    internal fun subgroupConflict(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory
            .forEachUniquePair(
                Lesson::class.java,
                Joiners.equal(Lesson::timeslot),
                Joiners.equal(Lesson::group),
                Joiners.equal(Lesson::subgroup)
            )
            .penalizeConfigurable()
            .asConstraint("Subgroup conflict")
    }

    internal fun divisionConflict(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory
            .forEachUniquePair(
                Lesson::class.java,
                Joiners.equal(Lesson::timeslot),
                Joiners.equal(Lesson::group),
                Joiners.filtering { first, second -> first.subgroup.division != second.subgroup.division }
            )
            .penalizeConfigurable()
            .asConstraint("Division conflict")
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
                    ).expand { _, _ -> 0 }
            ).groupBy(
                { _, group, _ -> group },
                collectAndThen(
                    toMap({ dayOfWeek, _, _ -> dayOfWeek }, { _, _, count -> count })
                ) { days -> days.mapValues { entry -> entry.value.first() } }
            ).expand { _, daysOfWeek ->
                val squaredSum = daysOfWeek.values.sumOf { it * it }
                val sum = daysOfWeek.values.sumOf { it }
                (-squaredSum + sum * sum * 1.0 / daysOfWeek.size).let {
                    if (it < 0) it + 1 else it
                }.roundToInt().absoluteValue
            }.filter { _, _, penalty -> penalty > 0 }
            .penalizeConfigurable { _, _, penalty -> penalty }
            .asConstraint("Evenly distributed lessons per day")
    }

    internal fun noGapsAtFirstTimeslotEachDay(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory.forEach(Timeslot::class.java)
            .groupBy(Timeslot::dayOfWeek, collectAndThen(toList()) { it.first() })
            .join(
                constraintFactory.forEach(Lesson::class.java)
                    .groupBy(Lesson::group)
            ).ifNotExists(
                Lesson::class.java,
                Joiners.equal({ _, _, group -> group }, { lesson -> lesson.group }),
                Joiners.equal({ _, timeslot, _ -> timeslot }, { lesson -> lesson.timeslot })
            )
            .penalizeConfigurable()
            .asConstraint("No gaps at first timeslot each day")
    }

    private fun subjectVarietyPerDay(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory.forEach(Lesson::class.java)
            .groupBy({ it.group to it.subgroup }, Lesson::subject, { it.timeslot!!.dayOfWeek }, count())
            .concat(
                constraintFactory.forEach(Lesson::class.java)
                    .groupBy({ it.group to it.subgroup }, Lesson::subject)
                    .join(
                        constraintFactory.forEach(Timeslot::class.java)
                            .groupBy(Timeslot::dayOfWeek)
                    ).ifNotExists(
                        Lesson::class.java,
                        Joiners.equal(
                            { groupToSubgroup, _, _ -> groupToSubgroup },
                            { lesson -> lesson.group to lesson.subgroup }),
                        Joiners.equal({ _, subject, _ -> subject }, Lesson::subject),
                        Joiners.equal({ _, _, dayOfWeek -> dayOfWeek }, { lesson -> lesson.timeslot!!.dayOfWeek })
                    ).expand { _, _, _ -> 0 }
            ).groupBy(
                { groupToSubgroup, _, _, _ -> groupToSubgroup },
                { _, subject, _, _ -> subject },
                collectAndThen(
                    toMap({ _, _, dayOfWeek, _ -> dayOfWeek }, { _, _, _, count -> count })
                ) { days -> days.mapValues { it.value.first() } }
            ).expand { _, _, daysOfWeek ->
                val squaredSum = daysOfWeek.values.sumOf { it * it }
                val sum = daysOfWeek.values.sumOf { it }
                (-squaredSum + sum * sum * 1.0 / daysOfWeek.size).let {
                    if (it < 0) it + 1 else it
                }.roundToInt().absoluteValue
            }.filter { _, _, _, penalty -> penalty > 0 }
            .penalizeConfigurable { _, _, _, penalty -> penalty }
            .asConstraint("Subject variety per day")
    }

    internal fun groupTimeEfficiency(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory.forEach(Lesson::class.java)
            .groupBy(
                Lesson::group,
                { it.timeslot?.dayOfWeek },
                collectAndThen(toList { lesson -> lesson.timeslot!! }) { timeslots ->
                    timeslots.sortedBy(Timeslot::start).zipWithNext()
                }
            ).flattenLast { it }
            .filter { _, _, timeslots ->
                Duration.between(timeslots.first.end, timeslots.second.start) >= Duration.ofMinutes(30)
            }
            .penalizeConfigurable()
            .asConstraint("Group time efficiency")
    }

    private fun dayComplexity(constraintFactory: ConstraintFactory): Constraint {
        // will not work as expected when will be more than two subgroups in division
        // and these subgroups will have different number of lessons, for example:
        // 8:00 [math group, chemistry group, history group]
        // 9:00 [math group, history group]
        // sum of groups' day complexity will be wrong: [math + math, chemistry + history, history + math]
        // sum of groups' day complexity should be: [math + math, chemistry, history + history]
        // this happened because solution is naive index-based, but it's ok for time of writing
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
                        Joiners.equal({ group, _ -> group }, { lesson -> lesson.group }),
                        Joiners.equal({ _, dayOfWeek -> dayOfWeek }, { lesson -> lesson.timeslot!!.dayOfWeek })
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
            .expand { _, daysOfWeek ->
                val subgroupsCount = daysOfWeek.values.maxOf(List<Int>::size)
                (0 until subgroupsCount).map { index ->
                    val squaredSum = daysOfWeek.values.sumOf {
                        val complexity = it.getOrNull(index) ?: it.first()
                        complexity * complexity
                    }
                    val sum = daysOfWeek.values.sumOf { it.getOrNull(index) ?: it.first() }
                    -squaredSum + sum * sum * 1.0 / daysOfWeek.size
                }.sumOf { (if (it < 0) it + 1 else it).roundToInt().absoluteValue }
            }.filter { _, _, penalty -> penalty > 0 }
            .penalizeConfigurable { _, _, penalty -> penalty }
            .asConstraint("Day complexity")
    }

    internal fun pairedSubjects(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory.forEach(PairedSubjects::class.java)
            .join(
                constraintFactory.forEach(PairedSubjects::class.java)
                    .flattenLast(PairedSubjects::groups)
                    .distinct(),
                Joiners.filtering { subjects, group -> subjects.groups.contains(group) }
            )
            .join(
                Lesson::class.java,
                Joiners.equal({ _, group -> group }, Lesson::group),
                Joiners.equal({ subjects, _ -> subjects.firstSubject }, Lesson::subject)
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
            .map({ group, _, _ -> group }, { _, subjects, _ -> subjects }, { _, subjects, lessons ->
                val allLessons = lessons.flatten()
                val firstLessons = allLessons.filter { it.subject == subjects.firstSubject }.sortedWith(
                    compareBy({ it.timeslot!!.dayOfWeek }, { it.timeslot!!.start })
                )
                val secondLessons = allLessons.filter { it.subject == subjects.secondSubject }.sortedWith(
                    compareBy({ it.timeslot!!.dayOfWeek }, { it.timeslot!!.start })
                )
                firstLessons.zip(secondLessons).take(subjects.count)
            }).flattenLast { it }
            .filter { _, _, (lesson1, lesson2) ->
                val between = Duration.between(lesson1.timeslot?.end, lesson2.timeslot?.start)
                lesson1.timeslot?.dayOfWeek != lesson2.timeslot?.dayOfWeek || between > Duration.ofMinutes(30)
            }
            .penalizeConfigurable()
            .asConstraint("Paired subjects")
    }

    internal fun disallowedSubjectsTimeslots(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory.forEach(Lesson::class.java)
            .join(SubjectDetail::class.java, Joiners.equal({ lesson -> lesson.subject.id }, SubjectDetail::id))
            .filter { lesson, subject -> subject.disallowedTimeslots.contains(lesson.timeslot) }
            .penalizeConfigurable()
            .asConstraint("Disallowed subjects timeslots")
    }

    private fun minDaysBetweenLessonsHard(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory.forEach(Lesson::class.java)
            .filter { it.subject.minDaysStrict }
            .groupBy({ it.group to it.subgroup }, Lesson::subject, toList { lesson -> lesson.timeslot!!.dayOfWeek })
            .expand { _, subject, dayOfWeeks ->
                dayOfWeeks.sortedBy(DayOfWeek::getValue)
                    .zipWithNext()
                    .sumOf { (first, second) ->
                        val diff = second.value - first.value
                        if (diff >= subject.minDaysBetween) 0 else subject.minDaysBetween - diff
                    }
            }.filter { _, _, _, penalty -> penalty > 0 }
            .penalizeConfigurable { _, _, _, penalty -> penalty }
            .asConstraint("Min days between lessons [hard]")
    }

    private fun minDaysBetweenLessonsSoft(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory.forEach(Lesson::class.java)
            .filter { !it.subject.minDaysStrict }
            .groupBy({ it.group to it.subgroup }, Lesson::subject, toList { lesson -> lesson.timeslot!!.dayOfWeek })
            .expand { _, subject, dayOfWeeks ->
                dayOfWeeks.sortedBy(DayOfWeek::getValue)
                    .zipWithNext()
                    .sumOf { (first, second) ->
                        val diff = second.value - first.value
                        if (diff >= subject.minDaysBetween) diff - subject.minDaysBetween else -(subject.minDaysBetween - diff)
                    }
            }.filter { _, _, _, penalty -> penalty != 0 }
            .impactConfigurable { _, _, _, penalty -> penalty }
            .asConstraint("Min days between lessons [soft]")
    }

    private fun onceFirstOrLastTimeslot(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory.forEach(Lesson::class.java)
            .groupBy(
                Lesson::group,
                { it.timeslot!!.dayOfWeek },
                min { lesson -> lesson.timeslot!!.start },
                max { lesson -> lesson.timeslot!!.start })
            .map { group, dayOfWeek, firstStart, lastStart -> Quadruple(group, dayOfWeek, firstStart, lastStart) }
            .join(
                constraintFactory.forEach(SubjectDetail::class.java)
                    .filter(SubjectDetail::onceFirstOrLastTimeslot),
            ).join(
                Lesson::class.java,
                Joiners.equal({ first, _ -> first.a }, Lesson::group),
                Joiners.equal({ _, subject -> subject.id }, { lesson -> lesson.subject.id })
            ).groupBy({ first, _, _ -> first.a },
                { _, subject, _ -> subject },
                sum { first, _, lesson -> if (lesson.timeslot!!.dayOfWeek == first.b && (lesson.timeslot.start == first.c || lesson.timeslot.start == first.d)) 1 else 0 })
            .filter { _, _, count -> count == 0 }
            .penalizeConfigurable()
            .asConstraint("Once first or last timeslot")
    }

    internal fun subgroupTimeEfficiency(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory.forEach(Lesson::class.java)
            .groupBy(
                Lesson::group,
                { it.subgroup.division },
                collectAndThen(toList()) { lessons -> lessons.groupBy(Lesson::subgroup) })
            .expand { _, _, subgroups ->
                val maxSubgroupLessons = subgroups.values.maxOf(List<Lesson>::size)
                (maxSubgroupLessons - subgroups.values.flatten().distinctBy { it.timeslot }.size).absoluteValue
            }.filter { _, _, _, penalty -> penalty > 0 }
            .penalizeConfigurable { _, _, _, penalty -> penalty }
            .asConstraint("Subgroup time efficiency")
    }

    private fun teacherTimeEfficiency(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory
            .forEach(Lesson::class.java)
            .join(
                Lesson::class.java, Joiners.equal(Lesson::teacher),
                Joiners.equal{ lesson: Lesson -> lesson.timeslot!!.dayOfWeek })
            .filter { lesson1: Lesson, lesson2: Lesson ->
                val between = Duration.between(
                    lesson1.timeslot!!.end,
                    lesson2.timeslot!!.start
                )
                !between.isNegative && between <= Duration.ofMinutes(30)
            }
            .rewardConfigurable()
            .asConstraint("Teacher time efficiency")
    }

    private fun subjectSequentialVariety(constraintFactory: ConstraintFactory): Constraint {
        return constraintFactory
            .forEach(Lesson::class.java)
            .join(
                Lesson::class.java,
                Joiners.equal(Lesson::subject),
                Joiners.equal(Lesson::group),
                Joiners.equal(Lesson::subgroup),
                Joiners.equal{ lesson: Lesson -> lesson.timeslot!!.dayOfWeek }
            )
            .filter { lesson1: Lesson, lesson2: Lesson ->
                val between = Duration.between(
                    lesson1.timeslot!!.end,
                    lesson2.timeslot!!.start
                )
                !between.isNegative && between <= Duration.ofMinutes(30)
            }
            .penalizeConfigurable()
            .asConstraint("Subject sequential variety")
    }
}