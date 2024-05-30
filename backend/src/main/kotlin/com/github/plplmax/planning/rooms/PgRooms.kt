package com.github.plplmax.planning.rooms

import com.github.plplmax.planning.database.tables.RoomsTable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class PgRooms(
    private val database: Database,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : Rooms {
    override suspend fun all(): List<Room> {
        return newSuspendedTransaction(dispatcher, database) {
            RoomsTable.selectAll()
                .orderBy(RoomsTable.name to SortOrder.ASC)
                .map(::toRoom)
        }
    }

    override suspend fun insert(room: NewRoom): Room {
        return newSuspendedTransaction(dispatcher, database) {
            RoomsTable.insert {
                it[name] = room.name
                it[capacity] = room.capacity
            }.resultedValues?.firstOrNull()?.let(::toRoom) ?: error("There is no result after inserting a new room")
        }
    }

    override suspend fun update(room: Room): Room {
        return newSuspendedTransaction(dispatcher, database) {
            RoomsTable.update({RoomsTable.id eq room.id}) {
                it[name] = room.name
                it[capacity] = room.capacity
            }.also { check(it > 0) {"There are no affected rows after updating room with id = ${room.id}"} }
            RoomsTable.select { RoomsTable.id eq room.id }.map(::toRoom).firstOrNull() ?: error("There is no room with id = ${room.id} after updating")
        }
    }

    override suspend fun delete(id: Int): Int {
        return newSuspendedTransaction(dispatcher, database) {
            RoomsTable.deleteWhere { RoomsTable.id eq id }
                .also {
                    check(it > 0) { "Database did not delete any rooms with id = $id" }
                }
        }
    }

    companion object {
        fun toRoom(row: ResultRow): Room = Room(
            id = row[RoomsTable.id].value,
            name = row[RoomsTable.name],
            capacity = row[RoomsTable.capacity]
        )
    }
}
