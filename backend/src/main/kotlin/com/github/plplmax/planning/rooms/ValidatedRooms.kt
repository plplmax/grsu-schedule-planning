package com.github.plplmax.planning.rooms

class ValidatedRooms(private val origin: Rooms) : Rooms by origin {
    override suspend fun insert(room: NewRoom): Room {
        val processedRoom = room.copy(name = processName(room.name))
        validateName(processedRoom.name)
        validateCapacity(room.capacity)
        return origin.insert(processedRoom)
    }

    override suspend fun update(room: Room): Room {
        val processedRoom = room.copy(name = processName(room.name))
        validateName(processedRoom.name)
        validateCapacity(room.capacity)
        return origin.update(processedRoom)
    }

    private fun processName(name: String): String = name.trim()

    private fun validateName(name: String) {
        check(name.length in 1..64) { "Room name should be from 1 to 64 characters" }
    }

    private fun validateCapacity(capacity: Int) {
        check(capacity in 1..5)
    }
}
