import http from '@/http'
import { LocalTime } from '@js-joda/core'

import type { TimeslotResponse } from './TimeslotResponse'
import type { Timeslot } from './Timeslot'

const all = () =>
  http.get<TimeslotResponse[]>('/timeslots').then(({ data }) => data.map(toTimeslot))

const add = (item: Timeslot) => {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const { id, ...data } = item
  return http.post<TimeslotResponse>('/timeslots', data).then(({ data }) => toTimeslot(data))
}

const edit = (item: Timeslot) => {
  const { id, ...data } = item
  return http.put<TimeslotResponse>(`/timeslots/${id}`, data).then(({ data }) => toTimeslot(data))
}

const remove = (id: number) => http.delete<number>(`/timeslots/${id}`).then(({ data }) => data)

const toTimeslot = (item: TimeslotResponse) =>
  <Timeslot>{
    ...item,
    start: LocalTime.parse(item.start),
    end: LocalTime.parse(item.end)
  }

export default { all, add, edit, remove }
