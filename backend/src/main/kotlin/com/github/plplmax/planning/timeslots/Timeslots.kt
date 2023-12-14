package com.github.plplmax.planning.timeslots

interface Timeslots {
    suspend fun all(): List<Timeslot>
    suspend fun insert(timeslots: List<Timeslot>)
    suspend fun delete(ids: List<Int>)
}
