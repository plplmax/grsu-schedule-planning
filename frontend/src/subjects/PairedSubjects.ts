import type { Group } from '@/groups/group'
import type { Subject } from './Subject'

export interface PairedSubjects {
  id: number
  firstSubject: Subject
  secondSubject: Subject
  count: number
  groups: Group[]
}

export interface PairedSubjectsUi {
  id: number
  firstSubject: Subject | null
  secondSubject: Subject | null
  count: number
  groups: Group[]
}
