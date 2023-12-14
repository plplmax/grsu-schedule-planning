@file:UseSerializers(LocalDateSerializer::class)

package com.github.plplmax.planning.timeslots

import com.github.plplmax.planning.plugins.serialization.LocalDateSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.stream.Stream

@Serializable
data class TimeslotsRangeOf(private val start: LocalDate, private val end: LocalDate) : TimeslotsRange {
    override suspend fun save(timeslots: Timeslots) {
        start.datesUntil(end.plusDays(1)).flatMap {
            Stream.of(
                Timeslot(
                    start = LocalDateTime.of(it, LocalTime.of(8, 30)),
                    end = LocalDateTime.of(it, LocalTime.of(9, 55))
                ),
                Timeslot(
                    start = LocalDateTime.of(it, LocalTime.of(10, 5)),
                    end = LocalDateTime.of(it, LocalTime.of(11, 30))
                ),
                Timeslot(
                    start = LocalDateTime.of(it, LocalTime.of(11, 40)),
                    end = LocalDateTime.of(it, LocalTime.of(13, 5))
                ),
                Timeslot(
                    start = LocalDateTime.of(it, LocalTime.of(13, 30)),
                    end = LocalDateTime.of(it, LocalTime.of(14, 55))
                ),
                Timeslot(
                    start = LocalDateTime.of(it, LocalTime.of(15, 5)),
                    end = LocalDateTime.of(it, LocalTime.of(16, 30))
                ),
                Timeslot(
                    start = LocalDateTime.of(it, LocalTime.of(16, 40)),
                    end = LocalDateTime.of(it, LocalTime.of(18, 5))
                ),
                Timeslot(
                    start = LocalDateTime.of(it, LocalTime.of(18, 15)),
                    end = LocalDateTime.of(it, LocalTime.of(19, 40))
                ),
                Timeslot(
                    start = LocalDateTime.of(it, LocalTime.of(19, 50)),
                    end = LocalDateTime.of(it, LocalTime.of(21, 15))
                )
            )
        }.toList().let { timeslots.insert(it) }
    }
}
