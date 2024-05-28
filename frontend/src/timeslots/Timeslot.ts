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
  shortLabel: string
}

export const dayOfWeeks: DayOfWeek[] = [
  {
    id: 1,
    label: 'Понедельник',
    shortLabel: 'Пн'
  },
  {
    id: 2,
    label: 'Вторник',
    shortLabel: 'Вт'
  },
  {
    id: 3,
    label: 'Среда',
    shortLabel: 'Ср'
  },
  {
    id: 4,
    label: 'Четверг',
    shortLabel: 'Чт'
  },
  {
    id: 5,
    label: 'Пятница',
    shortLabel: 'Пт'
  },
  {
    id: 6,
    label: 'Суббота',
    shortLabel: 'Сб'
  },
  {
    id: 7,
    label: 'Воскресенье',
    shortLabel: 'Вс'
  }
]
