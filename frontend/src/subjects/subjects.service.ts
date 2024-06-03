import http from '@/http'

import type { Subject, SubjectDetail } from './Subject'

const all = () => http.get<Subject[]>('/subjects').then(({ data }) => data)

const allDetails = () => http.get<SubjectDetail[]>('/subjects/details').then(({ data }) => data)

const add = (item: SubjectDetail) => {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const { id, ...data } = item
  return http.post<SubjectDetail>('/subjects', data).then(({ data }) => data)
}

const edit = (item: SubjectDetail) => {
  const { id, ...data } = item
  return http.put<SubjectDetail>(`/subjects/${id}`, data).then(({ data }) => data)
}

const remove = (id: number) => http.delete<number>(`/subjects/${id}`).then(({ data }) => data)

export default { all, allDetails, add, edit, remove }
