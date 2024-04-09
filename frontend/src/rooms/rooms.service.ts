import http from '@/http'

import type { Room } from './room'

const all = () => http.get<Room[]>('/rooms').then(({ data }) => data)

const add = (item: Room) => {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const { id, ...data } = item
  return http.post<Room>('/rooms', data).then(({ data }) => data)
}

const edit = (item: Room) => {
  const { id, ...data } = item
  return http.put<Room>(`/rooms/${id}`, data).then(({ data }) => data)
}

const remove = (id: number) => http.delete<number>(`/rooms/${id}`).then(({ data }) => data)

export default { all, add, edit, remove }
