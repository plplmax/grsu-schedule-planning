package com.github.plplmax.planning.teachers

import com.github.plplmax.planning.database.tables.SubjectsTable
import com.github.plplmax.planning.database.tables.TeachersTable
import com.github.plplmax.planning.database.tables.TeachersToSubjectsTable
import com.github.plplmax.planning.subjects.PgSubjects
import com.github.plplmax.planning.subjects.Subject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.SqlExpressionBuilder.notInList
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class PgTeachers(
    private val database: Database,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : Teachers {
    override suspend fun all(): List<Teacher> {
        return newSuspendedTransaction(dispatcher, database) {
            ((TeachersTable leftJoin TeachersToSubjectsTable) leftJoin SubjectsTable).selectAll()
                .orderBy(
                    TeachersTable.lastname to SortOrder.ASC,
                    TeachersTable.firstname to SortOrder.ASC,
                    SubjectsTable.name to SortOrder.ASC_NULLS_LAST
                )
                .let(::toTeachers)
        }
    }

    override suspend fun insert(teacher: NewTeacher): Teacher {
        return newSuspendedTransaction(dispatcher, database) {
            val id = TeachersTable.insertAndGetId {
                it[firstname] = teacher.firstname
                it[lastname] = teacher.lastname
            }.value

            TeachersToSubjectsTable.batchInsert(teacher.subjects) {
                this[TeachersToSubjectsTable.teacherId] = id
                this[TeachersToSubjectsTable.subjectId] = it.id
            }

            ((TeachersTable leftJoin TeachersToSubjectsTable) leftJoin SubjectsTable).select(TeachersTable.id eq id)
                .orderBy(SubjectsTable.name to SortOrder.ASC_NULLS_LAST)
                .let(::toTeachers)
                .firstOrNull() ?: error("There is no result after inserting a new teacher")
        }
    }

    override suspend fun update(teacher: Teacher): Teacher {
        return newSuspendedTransaction(dispatcher, database) {
            TeachersTable.update({ TeachersTable.id eq teacher.id }) {
                it[firstname] = teacher.firstname
                it[lastname] = teacher.lastname
            }.also { check(it > 0) { "There are no affected rows after updating teacher with id = ${teacher.id}" } }

            val oldTeacher =
                ((TeachersTable leftJoin TeachersToSubjectsTable) leftJoin SubjectsTable).select(TeachersTable.id eq teacher.id)
                    .let(::toTeachers)
                    .firstOrNull()
                    ?: error("There is no results after fetching subjects for teacher with id = ${teacher.id}")
            val addedSubjects = teacher.subjects - oldTeacher.subjects.toSet()
            val deletedSubjects = oldTeacher.subjects - teacher.subjects.toSet()

            TeachersToSubjectsTable.batchInsert(addedSubjects) {
                this[TeachersToSubjectsTable.teacherId] = teacher.id
                this[TeachersToSubjectsTable.subjectId] = it.id
            }

            TeachersToSubjectsTable.deleteWhere { subjectId inList deletedSubjects.map(Subject::id) }
                .also { check(it == deletedSubjects.size) { "Not all TeachersToSubjects rows were deleted. Expected = ${deletedSubjects.size}, actual = $it" } }

            ((TeachersTable leftJoin TeachersToSubjectsTable) leftJoin SubjectsTable).select(TeachersTable.id eq teacher.id)
                .orderBy(SubjectsTable.name to SortOrder.ASC_NULLS_LAST)
                .let(::toTeachers)
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

    private fun toTeachers(query: Query): List<Teacher> {
        val teachers = mutableMapOf<Int, Teacher>()

        query.forEach { row ->
            val teacherId = row[TeachersTable.id].value
            val subjectExists = row.getOrNull(TeachersToSubjectsTable.subjectId) != null
            val teacher = teachers[teacherId]?.let {
                it.copy(subjects = it.subjects + PgSubjects.toSubject(row))
            } ?: Teacher(
                id = teacherId,
                firstname = row[TeachersTable.firstname],
                lastname = row[TeachersTable.lastname],
                subjects = if (subjectExists) listOf(PgSubjects.toSubject(row)) else listOf()
            )
            teachers[teacherId] = teacher
        }

        return teachers.values.toList()
    }
}
