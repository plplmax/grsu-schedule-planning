import type { Lesson, LessonUi } from '@/lessons/lesson'

export interface Group {
  id: number
  number: number
  letter: string
}

export interface GroupDetail {
  id: number
  number: number
  letter: string
  lessons: Lesson[]
}

export interface GroupDetailUi {
  id: number
  number: number
  letter: string
  lessons: LessonUi[]
}
