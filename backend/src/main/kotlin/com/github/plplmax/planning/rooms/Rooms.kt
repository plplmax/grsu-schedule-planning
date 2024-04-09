package com.github.plplmax.planning.rooms

interface Rooms {
    suspend fun all(): List<Room>
    suspend fun insert(room: NewRoom): Room
    suspend fun update(room: Room): Room
    suspend fun delete(id: Int): Int
}
