package com.github.plplmax.planning.subjects

import com.github.plplmax.planning.database.tables.SubjectsTable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class PgSubjects(
    private val database: Database,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : Subjects {
    override suspend fun all(): List<Subject> {
        return newSuspendedTransaction(dispatcher, database) {
            SubjectsTable.selectAll()
                .orderBy(SubjectsTable.name to SortOrder.ASC)
                .map(::toSubject)
        }
    }

    override suspend fun insert(subject: NewSubject): Subject {
        return newSuspendedTransaction(dispatcher, database) {
            SubjectsTable.insert {
                it[name] = subject.name
            }.resultedValues?.firstOrNull()?.let(::toSubject) ?: error("There is no result after inserting a new subject")
        }
    }

    override suspend fun update(subject: Subject): Subject {
        return newSuspendedTransaction(dispatcher, database) {
            SubjectsTable.update({SubjectsTable.id eq subject.id}) {
                it[name] = subject.name
            }.also { check(it > 0) {"There are no affected rows after updating subject with id = ${subject.id}"} }
            SubjectsTable.select { SubjectsTable.id eq subject.id }.map(::toSubject).firstOrNull() ?: error("There is no subject with id = ${subject.id} after updating")
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

    private fun toSubject(row: ResultRow): Subject = Subject(
        id = row[SubjectsTable.id].value,
        name = row[SubjectsTable.name]
    )
}
