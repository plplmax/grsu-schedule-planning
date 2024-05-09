package com.github.plplmax.planning.groups

interface Groups {
    suspend fun all(): List<GroupDetail>
    suspend fun insert(group: NewGroup): GroupDetail
    suspend fun update(group: GroupDetail): GroupDetail
    suspend fun delete(id: Int): Int
}
