package com.github.plplmax.planning.timeslots

import com.github.plplmax.planning.database.tables.TimeslotsTable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class PgTimeslots(
    private val database: Database,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : Timeslots {
    override suspend fun all(): List<Timeslot> {
        return newSuspendedTransaction(dispatcher, database) {
            TimeslotsTable.selectAll()
                .sortedWith(
                    compareBy({ it[TimeslotsTable.start] }, { it[TimeslotsTable.end] }, { it[TimeslotsTable.id] })
                )
                .map(::toTimeslot)
        }
    }

    override suspend fun insert(timeslots: List<Timeslot>) {
        newSuspendedTransaction(dispatcher, database) {
            TimeslotsTable.batchInsert(timeslots) {
                this[TimeslotsTable.start] = it.start
                this[TimeslotsTable.end] = it.end
            }
        }
    }

    override suspend fun delete(ids: List<Int>) {
        newSuspendedTransaction(dispatcher, database) {
            TimeslotsTable.deleteWhere { TimeslotsTable.id inList ids }
                .also {
                    check(it == ids.size) { "Count of ids should be deleted is ${ids.size}, but database deletes $it" }
                }
        }
    }

    private fun toTimeslot(row: ResultRow): Timeslot = Timeslot(
        id = row[TimeslotsTable.id].value,
        start = row[TimeslotsTable.start],
        end = row[TimeslotsTable.end]
    )
}
