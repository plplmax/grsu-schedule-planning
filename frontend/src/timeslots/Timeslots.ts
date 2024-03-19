import type { Timeslot } from '@/timeslots/Timeslot'

export interface Timeslots {
  [date: string]: Timeslot[]
}
