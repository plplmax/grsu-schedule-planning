import http from '@/http'

import type { DivisionDetail } from './division'
import type { SubgroupDetail } from './subgroup'

const all = () => http.get<DivisionDetail[]>('/divisions').then(({ data }) => data)

const allSubgroups = () =>
  http.get<SubgroupDetail[]>('/divisions/subgroups').then(({ data }) => data)

const add = (item: DivisionDetail) => {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const { id, ...data } = item
  return http.post<DivisionDetail>('/divisions', data).then(({ data }) => data)
}

const edit = (item: DivisionDetail) => {
  const { id, ...data } = item
  return http.put<DivisionDetail>(`/divisions/${id}`, data).then(({ data }) => data)
}

const remove = (id: number) => http.delete<number>(`/divisions/${id}`).then(({ data }) => data)

export default { all, allSubgroups, add, edit, remove }
