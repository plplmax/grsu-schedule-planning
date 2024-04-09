package com.github.plplmax.planning.teachers

interface Teachers {
    suspend fun all(): List<Teacher>
    suspend fun insert(teacher: NewTeacher): Teacher
    suspend fun update(teacher: Teacher): Teacher
    suspend fun delete(id: Int): Int
    suspend fun exists(firstname: String, lastname: String, excludedIds: List<Int> = listOf()): Boolean
}
