import type { SubgroupDetail } from '@/divisions/subgroup'
import type { Group } from '@/groups/group'
import type { Room } from '@/rooms/room'
import type { Subject } from '@/subjects/Subject'
import type { Teacher } from '@/teachers/Teacher'
import type { Timeslot } from '@/timeslots/Timeslot'

export interface Lesson {
  id: number
  group: Group
  subgroup: SubgroupDetail
  teacher: Teacher
  subject: Subject
  room: Room
  timeslot: Timeslot | null
}

export interface LessonUi {
  id: number
  group: Group
  subgroup?: SubgroupDetail
  teacher?: Teacher
  subject: Subject
  room?: Room
  timeslot: Timeslot | null
}
