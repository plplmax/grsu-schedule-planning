package com.github.plplmax.planning.teachers

import com.github.plplmax.planning.database.tables.*
import com.github.plplmax.planning.lessons.Lesson
import com.github.plplmax.planning.lessons.PgLessons
import com.github.plplmax.planning.subjects.PgSubjects
import com.github.plplmax.planning.subjects.Subject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.util.*

class PgTeachers(
    private val database: Database,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : Teachers {
    override suspend fun all(): List<Teacher> {
        return newSuspendedTransaction(dispatcher, database) {
            teachersJoin()
                .selectAll()
                .orderBy(
                    TeachersTable.lastname to SortOrder.ASC,
                    TeachersTable.firstname to SortOrder.ASC,
                    SubjectsTable.name to SortOrder.ASC_NULLS_LAST,
                ).let(::toTeachers)
        }
    }

    override suspend fun allDetails(): List<TeacherDetail> {
        return newSuspendedTransaction(dispatcher, database) {
            teachersDetailJoin()
                .selectAll()
                .orderBy(
                    TeachersTable.lastname to SortOrder.ASC,
                    TeachersTable.firstname to SortOrder.ASC,
                    SubjectsTable.name to SortOrder.ASC_NULLS_LAST,
                    GroupsTable.number to SortOrder.DESC,
                    GroupsTable.letter to SortOrder.ASC,
                    SubgroupsTable.id to SortOrder.ASC,
                ).let(::toTeachersDetail)
        }
    }

    override suspend fun findById(id: Int): Optional<TeacherDetail> {
        return newSuspendedTransaction(dispatcher, database) {
            teachersDetailJoin()
                .select(TeachersTable.id eq id)
                .orderBy(
                    SubjectsTable.name to SortOrder.ASC,
                    GroupsTable.number to SortOrder.DESC,
                    GroupsTable.letter to SortOrder.ASC,
                    SubgroupsTable.id to SortOrder.ASC,
                ).let(::toTeachersDetail)
                .let { Optional.ofNullable(it.firstOrNull()) }
        }
    }

    override suspend fun insert(teacher: NewTeacher): TeacherDetail {
        return newSuspendedTransaction(dispatcher, database) {
            val id = TeachersTable.insertAndGetId {
                it[firstname] = teacher.firstname
                it[lastname] = teacher.lastname
            }.value

            TeachersToSubjectsTable.batchInsert(teacher.subjects) {
                this[TeachersToSubjectsTable.teacherId] = id
                this[TeachersToSubjectsTable.subjectId] = it.id
            }

            LessonsTable.batchInsert(teacher.lessons) {
                this[LessonsTable.groupId] = it.group.id
                this[LessonsTable.subgroupId] = it.subgroup.id
                this[LessonsTable.teacherId] = id
                this[LessonsTable.subjectId] = it.subject.id
                this[LessonsTable.roomId] = it.room.id
                this[LessonsTable.timeslotId] = it.timeslot?.id
            }

            teachersDetailJoin()
                .select(TeachersTable.id eq id)
                .orderBy(
                    SubjectsTable.name to SortOrder.ASC,
                    GroupsTable.number to SortOrder.DESC,
                    GroupsTable.letter to SortOrder.ASC,
                    SubgroupsTable.id to SortOrder.ASC,
                ).let(::toTeachersDetail)
                .firstOrNull() ?: error("There is no result after inserting a new teacher")
        }
    }

    override suspend fun update(teacher: TeacherDetail): TeacherDetail {
        return newSuspendedTransaction(dispatcher, database) {
            TeachersTable.update({ TeachersTable.id eq teacher.id }) {
                it[firstname] = teacher.firstname
                it[lastname] = teacher.lastname
            }.also { check(it > 0) { "There are no affected rows after updating teacher with id = ${teacher.id}" } }

            val oldTeacher = teachersJoin().select(TeachersTable.id eq teacher.id)
                .let(::toTeachers)
                .firstOrNull()
                ?: error("There is no results after fetching subjects for teacher with id = ${teacher.id}")
            val addedSubjects = teacher.subjects - oldTeacher.subjects.toSet()
            val deletedSubjects = oldTeacher.subjects - teacher.subjects.toSet()

            TeachersToSubjectsTable.batchInsert(addedSubjects) {
                this[TeachersToSubjectsTable.teacherId] = teacher.id
                this[TeachersToSubjectsTable.subjectId] = it.id
            }

            TeachersToSubjectsTable.deleteWhere {
                (teacherId eq teacher.id) and (subjectId inList deletedSubjects.map(Subject::id))
            }
                .also { check(it == deletedSubjects.size) { "Not all TeachersToSubjects rows were deleted. Expected = ${deletedSubjects.size}, actual = $it" } }

            teacher.lessons.forEach { lesson ->
                LessonsTable.update({ (LessonsTable.teacherId eq teacher.id) and (LessonsTable.id eq lesson.id) }) {
                    it[groupId] = lesson.group.id
                    it[subgroupId] = lesson.subgroup.id
                    it[subjectId] = lesson.subject.id
                    it[roomId] = lesson.room.id
                    it[timeslotId] = lesson.timeslot?.id
                }
            }

            val oldLessons = LessonsTable
                .innerJoin(GroupsTable)
                .innerJoin(SubgroupsTable)
                .innerJoin(DivisionsTable)
                .innerJoin(TeachersTable, onColumn = { LessonsTable.teacherId }, otherColumn = { TeachersTable.id })
                .innerJoin(SubjectsTable, onColumn = { LessonsTable.subjectId }, otherColumn = { SubjectsTable.id })
                .innerJoin(RoomsTable)
                .leftJoin(TimeslotsTable)
                .select { LessonsTable.teacherId eq teacher.id }
                .map(PgLessons::toLesson)

            val addedLessons = teacher.lessons - oldLessons.toSet()
            val deletedLessons = oldLessons - teacher.lessons.toSet()

            LessonsTable.batchInsert(addedLessons) {
                this[LessonsTable.groupId] = it.group.id
                this[LessonsTable.subgroupId] = it.subgroup.id
                this[LessonsTable.teacherId] = teacher.id
                this[LessonsTable.subjectId] = it.subject.id
                this[LessonsTable.roomId] = it.room.id
                this[LessonsTable.timeslotId] = it.timeslot?.id
            }

            LessonsTable.deleteWhere { LessonsTable.id inList deletedLessons.map(Lesson::id) }

            teachersDetailJoin()
                .select(TeachersTable.id eq teacher.id)
                .orderBy(
                    SubjectsTable.name to SortOrder.ASC,
                    GroupsTable.number to SortOrder.DESC,
                    GroupsTable.letter to SortOrder.ASC,
                    SubgroupsTable.id to SortOrder.ASC,
                ).let(::toTeachersDetail)
                .firstOrNull() ?: error("There is no result after updating a teacher")
        }
    }

    override suspend fun delete(id: Int): Int {
        return newSuspendedTransaction(dispatcher, database) {
            TeachersTable.deleteWhere { TeachersTable.id eq id }
                .also {
                    check(it > 0) { "Database did not delete any teachers with id = $id" }
                }
        }
    }

    override suspend fun exists(firstname: String, lastname: String, excludedIds: List<Int>): Boolean {
        return newSuspendedTransaction(dispatcher, database) {
            !TeachersTable.select(
                (TeachersTable.firstname eq firstname) and (TeachersTable.lastname eq lastname) and (TeachersTable.id notInList excludedIds)
            ).empty()
        }
    }

    private fun teachersJoin(): ColumnSet {
        return TeachersTable.leftJoin(TeachersToSubjectsTable)
            .leftJoin(SubjectsTable)
    }

    private fun teachersDetailJoin(): ColumnSet {
        return TeachersTable.leftJoin(TeachersToSubjectsTable)
            .leftJoin(
                LessonsTable,
                onColumn = { TeachersToSubjectsTable.teacherId },
                otherColumn = { teacherId },
                additionalConstraint = { TeachersToSubjectsTable.subjectId eq LessonsTable.subjectId })
            .leftJoin(SubjectsTable)
            .leftJoin(GroupsTable)
            .leftJoin(SubgroupsTable)
            .leftJoin(DivisionsTable)
            .leftJoin(RoomsTable)
            .leftJoin(TimeslotsTable)
    }

    private fun toTeachers(query: Query): List<Teacher> {
        val teachers = mutableMapOf<TeacherShort, List<Subject>>()

        query.forEach { row ->
            val teacher = toTeacherShort(row)
            val subject = row.getOrNull(SubjectsTable.id)?.let { PgSubjects.toSubject(row) }
            teachers.merge(teacher, listOfNotNull(subject)) { a, b -> a + b }
        }

        return teachers.map {
            Teacher(id = it.key.id, firstname = it.key.firstname, lastname = it.key.lastname, subjects = it.value)
        }
    }

    private fun toTeachersDetail(query: Query): List<TeacherDetail> {
        val subjects = mutableMapOf<TeacherShort, MutableList<Subject>>()
        val lessons = mutableMapOf<TeacherShort, MutableList<Lesson>>()

        query.forEach { row ->
            val teacher = toTeacherShort(row)
            subjects.computeIfAbsent(teacher) { mutableListOf() }
            lessons.computeIfAbsent(teacher) { mutableListOf() }
            row.getOrNull(SubjectsTable.id)?.let { subjects.getValue(teacher).add(PgSubjects.toSubject(row)) }
            row.getOrNull(LessonsTable.id)?.let { lessons.getValue(teacher).add(PgLessons.toLesson(row)) }
        }

        return subjects.keys.map {
            TeacherDetail(
                id = it.id,
                firstname = it.firstname,
                lastname = it.lastname,
                subjects = subjects.getValue(it).distinct(),
                lessons = lessons.getValue(it)
            )
        }
    }

    companion object {
        fun toTeacherShort(row: ResultRow): TeacherShort = TeacherShort(
            id = row[TeachersTable.id].value,
            firstname = row[TeachersTable.firstname],
            lastname = row[TeachersTable.lastname]
        )
    }
}
