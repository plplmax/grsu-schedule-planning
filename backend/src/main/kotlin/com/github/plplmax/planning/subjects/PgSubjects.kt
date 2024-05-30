package com.github.plplmax.planning.subjects

import com.github.plplmax.planning.database.tables.SubjectsTable
import com.github.plplmax.planning.database.tables.TimeslotsTable
import com.github.plplmax.planning.database.tables.TimeslotsToSubjectsTable
import com.github.plplmax.planning.timeslots.PgTimeslots
import com.github.plplmax.planning.timeslots.Timeslot
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class PgSubjects(
    private val database: Database,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : Subjects {
    override suspend fun all(): List<SubjectDetail> {
        return newSuspendedTransaction(dispatcher, database) {
            SubjectsTable.leftJoin(TimeslotsToSubjectsTable)
                .leftJoin(TimeslotsTable)
                .selectAll()
                .orderBy(
                    SubjectsTable.name to SortOrder.ASC,
                    TimeslotsTable.dayOfWeek to SortOrder.ASC,
                    TimeslotsTable.start to SortOrder.ASC,
                    TimeslotsTable.end to SortOrder.ASC
                )
                .let(::toSubjectDetails)
        }
    }

    override suspend fun insert(subject: NewSubject): SubjectDetail {
        return newSuspendedTransaction(dispatcher, database) {
            val id = SubjectsTable.insertAndGetId {
                it[name] = subject.name
                it[complexity] = subject.complexity
                it[minDaysBetween] = subject.minDaysBetween
                it[minDaysStrict] = subject.minDaysStrict
                it[onceFirstOrLastTimeslot] = subject.onceFirstOrLastTimeslot
            }

            TimeslotsToSubjectsTable.batchInsert(subject.disallowedTimeslots) {
                this[TimeslotsToSubjectsTable.timeslotId] = it.id
                this[TimeslotsToSubjectsTable.subjectId] = id
            }

            SubjectsTable.leftJoin(TimeslotsToSubjectsTable)
                .leftJoin(TimeslotsTable)
                .select { SubjectsTable.id eq id }
                .orderBy(
                    TimeslotsTable.dayOfWeek to SortOrder.ASC,
                    TimeslotsTable.start to SortOrder.ASC,
                    TimeslotsTable.end to SortOrder.ASC
                )
                .let(::toSubjectDetails)
                .firstOrNull() ?: error("There is no result after inserting a new subject")
        }
    }

    override suspend fun update(subject: SubjectDetail): SubjectDetail {
        return newSuspendedTransaction(dispatcher, database) {
            SubjectsTable.update({ SubjectsTable.id eq subject.id }) {
                it[name] = subject.name
                it[complexity] = subject.complexity
                it[minDaysBetween] = subject.minDaysBetween
                it[minDaysStrict] = subject.minDaysStrict
                it[onceFirstOrLastTimeslot] = subject.onceFirstOrLastTimeslot
            }.also { check(it > 0) { "There are no affected rows after updating subject with id = ${subject.id}" } }

            val timeslots = TimeslotsToSubjectsTable.innerJoin(TimeslotsTable)
                .select { TimeslotsToSubjectsTable.subjectId eq subject.id }
                .map(PgTimeslots::toTimeslot)

            val added = subject.disallowedTimeslots - timeslots.toSet()
            val deleted = timeslots - subject.disallowedTimeslots.toSet()

            TimeslotsToSubjectsTable.batchInsert(added) {
                this[TimeslotsToSubjectsTable.timeslotId] = it.id
                this[TimeslotsToSubjectsTable.subjectId] = subject.id
            }

            TimeslotsToSubjectsTable.deleteWhere {
                (subjectId eq subject.id) and (timeslotId inList deleted.map(Timeslot::id))
            }

            SubjectsTable.leftJoin(TimeslotsToSubjectsTable)
                .leftJoin(TimeslotsTable)
                .select { SubjectsTable.id eq subject.id }
                .orderBy(
                    TimeslotsTable.dayOfWeek to SortOrder.ASC,
                    TimeslotsTable.start to SortOrder.ASC,
                    TimeslotsTable.end to SortOrder.ASC
                )
                .let(::toSubjectDetails)
                .firstOrNull() ?: error("There is no subject with id = ${subject.id} after updating")
        }
    }

    override suspend fun delete(id: Int): Int {
        return newSuspendedTransaction(dispatcher, database) {
            SubjectsTable.deleteWhere { SubjectsTable.id eq id }
                .also {
                    check(it > 0) { "Database did not delete any subjects with id = $id" }
                }
        }
    }

    companion object {
        fun toSubject(row: ResultRow): Subject = Subject(
            id = row[SubjectsTable.id].value,
            name = row[SubjectsTable.name],
            complexity = row[SubjectsTable.complexity],
            minDaysBetween = row[SubjectsTable.minDaysBetween],
            minDaysStrict = row[SubjectsTable.minDaysStrict],
            onceFirstOrLastTimeslot = row[SubjectsTable.onceFirstOrLastTimeslot]
        )

        fun toSubjectDetails(query: Query): List<SubjectDetail> {
            val subjects = mutableMapOf<Subject, List<Timeslot>>()

            query.forEach { row ->
                val subject = toSubject(row)
                val timeslot = row.getOrNull(TimeslotsTable.id)?.let { PgTimeslots.toTimeslot(row) }
                subjects.merge(subject, listOfNotNull(timeslot)) { a, b -> a + b }
            }

            return subjects.map {
                SubjectDetail(
                    id = it.key.id,
                    name = it.key.name,
                    complexity = it.key.complexity,
                    disallowedTimeslots = it.value,
                    minDaysBetween = it.key.minDaysBetween,
                    minDaysStrict = it.key.minDaysStrict,
                    onceFirstOrLastTimeslot = it.key.onceFirstOrLastTimeslot
                )
            }
        }
    }
}
