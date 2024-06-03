package com.github.plplmax.planning.subjects

interface Subjects {
    suspend fun all(): List<Subject>
    suspend fun allDetails(): List<SubjectDetail>
    suspend fun insert(subject: NewSubject): SubjectDetail
    suspend fun update(subject: SubjectDetail): SubjectDetail
    suspend fun delete(id: Int): Int
}
