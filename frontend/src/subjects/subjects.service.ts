import http from '@/http'

import type { Subject } from './Subject'

const all = () => http.get<Subject[]>('/subjects').then(({ data }) => data)

const add = (item: Subject) => {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const { id, ...data } = item
  return http.post<Subject>('/subjects', data).then(({ data }) => data)
}

const edit = (item: Subject) => {
  const { id, ...data } = item
  return http.put<Subject>(`/subjects/${id}`, data).then(({ data }) => data)
}

const remove = (id: number) => http.delete<number>(`/subjects/${id}`).then(({ data }) => data)

export default { all, add, edit, remove }
