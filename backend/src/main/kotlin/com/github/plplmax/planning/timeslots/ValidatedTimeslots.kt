package com.github.plplmax.planning.timeslots

import java.time.LocalTime

class ValidatedTimeslots(private val origin: Timeslots) : Timeslots by origin {
    override suspend fun insert(timeslot: NewTimeslot): Timeslot {
        validateDuration(timeslot.start, timeslot.end)
        validateTimeCrossings(timeslot.start, timeslot.end, origin.all().filter { it.dayOfWeek == timeslot.dayOfWeek })
        return origin.insert(timeslot)
    }

    override suspend fun update(timeslot: Timeslot): Timeslot {
        validateDuration(timeslot.start, timeslot.end)
        validateTimeCrossings(timeslot.start, timeslot.end, origin.all().filter { it.dayOfWeek == timeslot.dayOfWeek && it.id != timeslot.id })
        return origin.update(timeslot)
    }

    private fun validateDuration(start: LocalTime, end: LocalTime) {
        if (start >= end) error("`start` = $start should be less than `end` = $end")
    }

    private fun validateTimeCrossings(start: LocalTime, end: LocalTime, timeslots: List<Timeslot>) {
        if (timeslots.any { start >= it.start && start <= it.end || end >= it.start && end <= it.end}) {
            error("Time with `start` = $start and `end` = $end has crossings with any other times")
        }
    }
}