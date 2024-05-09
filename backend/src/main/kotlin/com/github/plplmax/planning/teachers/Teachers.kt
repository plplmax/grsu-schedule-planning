package com.github.plplmax.planning.teachers

interface Teachers {
    suspend fun all(): List<TeacherDetail>
    suspend fun insert(teacher: NewTeacher): TeacherDetail
    suspend fun update(teacher: TeacherDetail): TeacherDetail
    suspend fun delete(id: Int): Int
    suspend fun exists(firstname: String, lastname: String, excludedIds: List<Int> = listOf()): Boolean
}
