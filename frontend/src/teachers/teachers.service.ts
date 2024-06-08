import http from '@/http'

import type { Teacher, TeacherDetail } from './Teacher'

const all = () => http.get<Teacher[]>('/teachers').then(({ data }) => data)

const allDetails = () => http.get<TeacherDetail[]>('/teachers/details').then(({ data }) => data)

const findById = (id: number) => http.get<TeacherDetail>(`/teachers/${id}`).then(({ data }) => data)

const add = (item: TeacherDetail) => {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const { id, ...data } = item
  return http.post<TeacherDetail>('/teachers', data).then(({ data }) => data)
}

const edit = (item: TeacherDetail) => {
  const { id, ...data } = item
  return http.put<TeacherDetail>(`/teachers/${id}`, data).then(({ data }) => data)
}

const remove = (id: number) => http.delete<number>(`/teachers/${id}`).then(({ data }) => data)

export default { all, allDetails, findById, add, edit, remove }
