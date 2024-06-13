package com.github.plplmax.planning.timetable

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier
import com.github.plplmax.planning.divisions.Division
import com.github.plplmax.planning.divisions.SubgroupDetail
import com.github.plplmax.planning.groups.Group
import com.github.plplmax.planning.lessons.Lesson
import com.github.plplmax.planning.rooms.Room
import com.github.plplmax.planning.subjects.Subject
import com.github.plplmax.planning.subjects.SubjectDetail
import com.github.plplmax.planning.subjects.paired.PairedSubjects
import com.github.plplmax.planning.teachers.TeacherShort
import com.github.plplmax.planning.timeslots.Timeslot
import java.time.DayOfWeek
import java.time.LocalTime
import kotlin.test.BeforeTest
import kotlin.test.Test

class TimetableConstraintProviderTest {

    private lateinit var verifier: ConstraintVerifier<TimetableConstraintProvider, Timetable>
    private val firstTimeslot = Timeslot(1, DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(8, 45))
    private val secondTimeslot = Timeslot(2, DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(9, 45))
    private val thirdTimeslot = Timeslot(3, DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(10, 45))
    private val fourthTimeslot = Timeslot(4, DayOfWeek.MONDAY, LocalTime.of(11, 0), LocalTime.of(11, 45))

    @BeforeTest
    fun setUp() {
        verifier = ConstraintVerifier.build(TimetableConstraintProvider(), Timetable::class.java, Lesson::class.java)
    }

    @Test
    fun teacherConflict() {
        val firstLesson =
            Lesson(id = 1, teacher = TeacherShort(id = 1, firstname = "A", lastname = "A"), timeslot = firstTimeslot)
        val secondLesson =
            Lesson(id = 2, teacher = TeacherShort(id = 2, firstname = "B", lastname = "B"), timeslot = secondTimeslot)
        val conflictedLesson =
            Lesson(id = 3, teacher = TeacherShort(id = 1, firstname = "A", lastname = "A"), timeslot = firstTimeslot)
        verifier.verifyThat(TimetableConstraintProvider::teacherConflict)
            .given(firstLesson, secondLesson, conflictedLesson)
            .penalizesBy(1)
    }

    @Test
    fun subgroupConflict() {
        val conflictedSubgroup = SubgroupDetail(id = 1, name = "English 1", division = Division(id = 1))
        val firstLesson = Lesson(id = 1, subgroup = conflictedSubgroup, timeslot = firstTimeslot)
        val secondLesson = Lesson(
            id = 2,
            subgroup = SubgroupDetail(id = 2, name = "English 2", division = Division(id = 1)),
            timeslot = firstTimeslot
        )
        val thirdLesson = Lesson(
            id = 3,
            subgroup = SubgroupDetail(id = 3, name = "Whole class", division = Division(id = 2)),
            timeslot = secondTimeslot
        )
        val conflictedLesson = Lesson(id = 4, subgroup = conflictedSubgroup, timeslot = firstTimeslot)
        verifier.verifyThat(TimetableConstraintProvider::subgroupConflict)
            .given(firstLesson, secondLesson, thirdLesson, conflictedLesson)
            .penalizesBy(1)
    }

    @Test
    fun divisionConflict() {
        val firstLesson = Lesson(
            id = 1,
            subgroup = SubgroupDetail(id = 1, name = "English 1", division = Division(id = 1)),
            timeslot = firstTimeslot
        )
        val secondLesson = Lesson(
            id = 2,
            subgroup = SubgroupDetail(id = 2, name = "English 2", division = Division(id = 1)),
            timeslot = firstTimeslot
        )
        val thirdLesson = Lesson(
            id = 3,
            subgroup = SubgroupDetail(id = 1, name = "English 1", division = Division(id = 1)),
            timeslot = secondTimeslot
        )
        val conflictedLesson = Lesson(
            id = 4,
            subgroup = SubgroupDetail(id = 3, name = "Whole class", division = Division(id = 2)),
            timeslot = secondTimeslot
        )
        verifier.verifyThat(TimetableConstraintProvider::divisionConflict)
            .given(firstLesson, secondLesson, thirdLesson, conflictedLesson)
            .penalizesBy(1)
    }

    @Test
    fun roomCapacity() {
        val conflictedRoom = Room(id = 1, name = "200", capacity = 1)
        val firstLesson = Lesson(id = 1, room = conflictedRoom, timeslot = firstTimeslot)
        val secondLesson = Lesson(id = 2, room = Room(id = 2, name = "300", capacity = 1), timeslot = secondTimeslot)
        val firstConflictedLesson = Lesson(id = 3, room = conflictedRoom, timeslot = firstTimeslot)
        val secondConflictedLesson = Lesson(id = 4, room = conflictedRoom, timeslot = firstTimeslot)
        verifier.verifyThat(TimetableConstraintProvider::roomCapacity)
            .given(firstLesson, secondLesson, firstConflictedLesson, secondConflictedLesson)
            .penalizesBy(2)
    }

    @Test
    fun noGapsAtFirstTimeslotEachDay() {
        val timeslots = arrayOf(
            firstTimeslot.copy(id = 1, dayOfWeek = DayOfWeek.MONDAY),
            secondTimeslot.copy(id = 2, dayOfWeek = DayOfWeek.MONDAY),
            firstTimeslot.copy(id = 3, dayOfWeek = DayOfWeek.TUESDAY)
        )
        val firstLesson = Lesson(id = 1, timeslot = timeslots[1])
        val secondLesson = Lesson(id = 2, timeslot = timeslots[2])
        verifier.verifyThat(TimetableConstraintProvider::noGapsAtFirstTimeslotEachDay)
            .given(firstLesson, secondLesson, *timeslots)
            .penalizesBy(1)
    }

    @Test
    fun groupTimeEfficiency() {
        val group = Group(id = 1, number = 5, letter = 'A')
        val firstLesson = Lesson(id = 1, group = group, timeslot = firstTimeslot)
        val secondLesson = Lesson(id = 2, group = group, timeslot = thirdTimeslot)
        val thirdLesson = Lesson(id = 3, group = group, timeslot = fourthTimeslot)
        verifier.verifyThat(TimetableConstraintProvider::groupTimeEfficiency)
            .given(firstLesson, secondLesson, thirdLesson)
            .penalizesBy(1)
    }

    @Test
    fun subgroupTimeEfficiency() {
        val firstLesson = Lesson(
            id = 1,
            subgroup = SubgroupDetail(id = 1, name = "English 1", division = Division(id = 1)),
            timeslot = firstTimeslot
        )
        val secondLesson = Lesson(
            id = 2,
            subgroup = SubgroupDetail(id = 2, name = "English 2", division = Division(id = 1)),
            timeslot = firstTimeslot
        )
        val nonEfficientLesson = Lesson(
            id = 3,
            subgroup = SubgroupDetail(id = 3, name = "English 3", division = Division(id = 1)),
            timeslot = secondTimeslot
        )
        verifier.verifyThat(TimetableConstraintProvider::subgroupTimeEfficiency)
            .given(firstLesson, secondLesson, nonEfficientLesson)
            .penalizesBy(1)
    }

    @Test
    fun disallowedSubjectsTimeslots() {
        val subjectDetail = SubjectDetail(
            id = 1,
            name = "Math",
            complexity = 10,
            disallowedTimeslots = listOf(firstTimeslot),
            minDaysBetween = 1,
            minDaysStrict = false,
            onceFirstOrLastTimeslot = false
        )
        val subject = Subject(
            id = 1,
            name = "Math",
            complexity = 10,
            minDaysBetween = 1,
            minDaysStrict = false,
            onceFirstOrLastTimeslot = false
        )
        val firstLesson = Lesson(id = 1, subject = subject, timeslot = firstTimeslot)
        val secondLesson = Lesson(id = 2, subject = subject, timeslot = secondTimeslot)
        val thirdLesson = Lesson(id = 3, subject = subject, timeslot = thirdTimeslot)
        verifier.verifyThat(TimetableConstraintProvider::disallowedSubjectsTimeslots)
            .given(firstLesson, secondLesson, thirdLesson, subjectDetail)
            .penalizesBy(1)
    }

    @Test
    fun pairedSubjects() {
        val groupA = Group(id = 1, number = 5, letter = 'A')
        val groupB = Group(id = 2, number = 5, letter = 'B')
        val math = Subject(
            id = 1,
            name = "Math",
            complexity = 10,
            minDaysBetween = 1,
            minDaysStrict = false,
            onceFirstOrLastTimeslot = false
        )
        val physic = Subject(
            id = 2,
            name = "Physic",
            complexity = 10,
            minDaysBetween = 1,
            minDaysStrict = false,
            onceFirstOrLastTimeslot = false
        )
        val pairedSubjects =
            PairedSubjects(id = 1, firstSubject = math, secondSubject = physic, groups = listOf(groupA), count = 1)
        val groupAFirstLesson = Lesson(id = 1, group = groupA, subject = math, timeslot = firstTimeslot)
        val groupASecondLesson = Lesson(id = 2, group = groupA, subject = physic, timeslot = thirdTimeslot)
        val groupBFirstLesson = Lesson(id = 3, group = groupB, subject = math, timeslot = firstTimeslot)
        val groupBSecondLesson = Lesson(id = 4, group = groupB, subject = physic, timeslot = thirdTimeslot)
        verifier.verifyThat(TimetableConstraintProvider::pairedSubjects)
            .given(groupAFirstLesson, groupASecondLesson, groupBFirstLesson, groupBSecondLesson, pairedSubjects)
            .penalizesBy(1)
    }
}
