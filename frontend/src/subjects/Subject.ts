import type { Timeslot } from '@/timeslots/Timeslot'

export interface Subject {
  id: number
  name: string
  complexity: number
  minDaysBetween: number
  minDaysStrict: boolean
  onceFirstOrLastTimeslot: boolean
}

export interface SubjectDetail {
  id: number
  name: string
  complexity: number
  disallowedTimeslots: Timeslot[]
  minDaysBetween: number
  minDaysStrict: boolean
  onceFirstOrLastTimeslot: boolean
}
