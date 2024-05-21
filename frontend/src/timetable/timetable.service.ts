import http from '@/http'

import type { TimetableStatusResponse } from './TimetableStatus'

const status = () =>
  http.get<TimetableStatusResponse>('/timetable/status').then(({ data }) => data.status)

const start = () => http.post('/timetable/solve')

const stop = () => http.post('/timetable/stopSolving')

export default { status, start, stop }
