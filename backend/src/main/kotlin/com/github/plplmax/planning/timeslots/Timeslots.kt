package com.github.plplmax.planning.timeslots

interface Timeslots {
    suspend fun all(): List<Timeslot>
    suspend fun insert(timeslot: NewTimeslot): Timeslot
    suspend fun update(timeslot: Timeslot): Timeslot
    suspend fun delete(id: Int): Int
}
