package com.github.plplmax.planning.lessons

interface Lessons {
    suspend fun all(): List<Lesson>
    suspend fun update(lessons: List<Lesson>)
}
