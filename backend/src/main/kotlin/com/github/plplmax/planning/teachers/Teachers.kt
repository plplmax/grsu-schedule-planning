package com.github.plplmax.planning.teachers

import java.util.*

interface Teachers {
    suspend fun all(): List<Teacher>
    suspend fun allDetails(): List<TeacherDetail>
    suspend fun findById(id: Int): Optional<TeacherDetail>
    suspend fun insert(teacher: NewTeacher): TeacherDetail
    suspend fun update(teacher: TeacherDetail): TeacherDetail
    suspend fun delete(id: Int): Int
    suspend fun exists(firstname: String, lastname: String, excludedIds: List<Int> = listOf()): Boolean
}
