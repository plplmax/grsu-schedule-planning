package com.github.plplmax.planning.timeslots

interface TimeslotsRange {
    suspend fun save(timeslots: Timeslots)
}
