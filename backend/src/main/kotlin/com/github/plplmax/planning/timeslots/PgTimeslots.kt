package com.github.plplmax.planning.timeslots

import com.github.plplmax.planning.database.tables.TimeslotsTable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class PgTimeslots(
    private val database: Database,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : Timeslots {
    override suspend fun all(): List<Timeslot> {
        return newSuspendedTransaction(dispatcher, database) {
            TimeslotsTable.selectAll()
                .sortedWith(
                    compareBy({it[TimeslotsTable.dayOfWeek]},{ it[TimeslotsTable.start] }, { it[TimeslotsTable.end] }, { it[TimeslotsTable.id] })
                )
                .map(::toTimeslot)
        }
    }

    override suspend fun insert(timeslot: NewTimeslot): Timeslot {
        return newSuspendedTransaction(dispatcher, database) {
            TimeslotsTable.insert {
                it[dayOfWeek] = timeslot.dayOfWeek
                it[start] = timeslot.start
                it[end] = timeslot.end
            }.resultedValues?.firstOrNull()?.let(::toTimeslot) ?: error("There is no result after inserting a new timeslot")
        }
    }

    override suspend fun update(timeslot: Timeslot): Timeslot {
        return newSuspendedTransaction(dispatcher, database) {
            TimeslotsTable.update({TimeslotsTable.id eq timeslot.id}) {
                it[dayOfWeek] = timeslot.dayOfWeek
                it[start] = timeslot.start
                it[end] = timeslot.end
            }.also { check(it > 0) {"There are no affected rows after updating timeslot with id = ${timeslot.id}"} }
            TimeslotsTable.select { TimeslotsTable.id eq timeslot.id }.map(::toTimeslot).firstOrNull() ?: error("There is no timeslot with id = ${timeslot.id} after updating")
        }
    }

    override suspend fun delete(id: Int): Int {
        return newSuspendedTransaction(dispatcher, database) {
            TimeslotsTable.deleteWhere { TimeslotsTable.id eq id }
                .also {
                    check(it > 0) { "Database did not delete any timeslots with id = $id" }
                }
        }
    }

    companion object {
        fun toTimeslot(row: ResultRow): Timeslot = Timeslot(
            id = row[TimeslotsTable.id].value,
            dayOfWeek = row[TimeslotsTable.dayOfWeek],
            start = row[TimeslotsTable.start],
            end = row[TimeslotsTable.end]
        )
    }
}
