package com.github.plplmax.planning.groups

import java.util.*

interface Groups {
    suspend fun all(): List<Group>
    suspend fun allDetails(): List<GroupDetail>
    suspend fun findById(id: Int): Optional<GroupDetail>
    suspend fun insert(group: NewGroup): GroupDetail
    suspend fun update(group: GroupDetail): GroupDetail
    suspend fun delete(id: Int): Int
}
