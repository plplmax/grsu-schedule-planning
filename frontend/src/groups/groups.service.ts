import http from '@/http'

import type { Group, GroupDetail } from './group'

const all = () => http.get<Group[]>('/groups').then(({ data }) => data)

const allDetails = () => http.get<GroupDetail[]>('/groups/details').then(({ data }) => data)

const findById = (id: number) => http.get<GroupDetail>(`/groups/${id}`).then(({ data }) => data)

const add = (item: GroupDetail) => {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const { id, ...data } = item
  return http.post<GroupDetail>('/groups', data).then(({ data }) => data)
}

const edit = (item: GroupDetail) => {
  const { id, ...data } = item
  return http.put<GroupDetail>(`/groups/${id}`, data).then(({ data }) => data)
}

const remove = (id: number) => http.delete<number>(`/groups/${id}`).then(({ data }) => data)

export default { all, allDetails, findById, add, edit, remove }
