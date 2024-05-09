import type { Subject } from '@/subjects/Subject'

export interface Teacher {
  id: number
  firstname: string
  lastname: string
}

export interface TeacherDetail {
  id: number
  firstname: string
  lastname: string
  subjects: Subject[]
}
