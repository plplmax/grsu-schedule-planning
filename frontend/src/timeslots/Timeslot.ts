import type { LocalTime } from '@js-joda/core'

export interface Timeslot {
  id: number
  dayOfWeek: number
  start: LocalTime
  end: LocalTime
}

export interface NewTimeslot {
  dayOfWeek: number
  start: LocalTime
  end: LocalTime
}

export interface DayOfWeek {
  id: number
  label: string
}
