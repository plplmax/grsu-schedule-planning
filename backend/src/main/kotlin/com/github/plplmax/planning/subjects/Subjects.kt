package com.github.plplmax.planning.subjects

interface Subjects {
    suspend fun all(): List<Subject>
    suspend fun insert(subject: NewSubject): Subject
    suspend fun update(subject: Subject): Subject
    suspend fun delete(id: Int): Int
}
