import http from '@/http'

import type { PairedSubjects } from './PairedSubjects'

const all = () => http.get<PairedSubjects[]>('/subjects/paired').then(({ data }) => data)

const add = (item: PairedSubjects) => {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const { id, ...data } = item
  return http.post<PairedSubjects>('/subjects/paired', data).then(({ data }) => data)
}

const edit = (item: PairedSubjects) => {
  const { id, ...data } = item
  return http.put<PairedSubjects>(`/subjects/paired/${id}`, data).then(({ data }) => data)
}

const remove = (id: number) =>
  http.delete<number>(`/subjects/paired/${id}`).then(({ data }) => data)

export default { all, add, edit, remove }
