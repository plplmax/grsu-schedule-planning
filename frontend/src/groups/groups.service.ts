import http from '@/http'

import type { GroupDetail } from './group'

const all = () => http.get<GroupDetail[]>('/groups').then(({ data }) => data)

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

export default { all, add, edit, remove }
