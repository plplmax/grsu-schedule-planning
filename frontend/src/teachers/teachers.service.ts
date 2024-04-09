import http from '@/http'

import type { Teacher } from './Teacher'

const all = () => http.get<Teacher[]>('/teachers').then(({ data }) => data)

const add = (item: Teacher) => {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const { id, ...data } = item
  return http.post<Teacher>('/teachers', data).then(({ data }) => data)
}

const edit = (item: Teacher) => {
  const { id, ...data } = item
  return http.put<Teacher>(`/teachers/${id}`, data).then(({ data }) => data)
}

const remove = (id: number) => http.delete<number>(`/teachers/${id}`).then(({ data }) => data)

export default { all, add, edit, remove }
