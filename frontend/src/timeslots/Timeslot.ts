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

export const dayOfWeeks: DayOfWeek[] = [
  {
    id: 1,
    label: 'Понедельник'
  },
  {
    id: 2,
    label: 'Вторник'
  },
  {
    id: 3,
    label: 'Среда'
  },
  {
    id: 4,
    label: 'Четверг'
  },
  {
    id: 5,
    label: 'Пятница'
  },
  {
    id: 6,
    label: 'Суббота'
  },
  {
    id: 7,
    label: 'Воскресенье'
  }
]
