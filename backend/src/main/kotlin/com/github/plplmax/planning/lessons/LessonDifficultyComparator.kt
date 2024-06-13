package com.github.plplmax.planning.lessons

class LessonDifficultyComparator : Comparator<Lesson> {
    override fun compare(o1: Lesson, o2: Lesson): Int {
        return o1.subgroup.id.compareTo(o2.subgroup.id)
    }
}
