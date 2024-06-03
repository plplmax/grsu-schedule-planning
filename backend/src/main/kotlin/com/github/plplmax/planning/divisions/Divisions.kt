package com.github.plplmax.planning.divisions

interface Divisions {
    suspend fun all(): List<DivisionDetail>
    suspend fun allSubgroups(): List<SubgroupDetail>
    suspend fun insert(division: NewDivision): DivisionDetail
    suspend fun update(division: DivisionDetail): DivisionDetail
    suspend fun delete(id: Int): Int
}
