import type { Lesson, LessonUi } from '@/lessons/lesson'
import type { Subject } from '@/subjects/Subject'

export interface TeacherShort {
  id: number
  firstname: string
  lastname: string
}

export interface Teacher {
  id: number
  firstname: string
  lastname: string
  subjects: Subject[]
}

export interface TeacherDetail {
  id: number
  firstname: string
  lastname: string
  subjects: Subject[]
  lessons: Lesson[]
}

export interface TeacherDetailUi {
  id: number
  firstname: string
  lastname: string
  subjects: Subject[]
  lessons: LessonUi[]
}
