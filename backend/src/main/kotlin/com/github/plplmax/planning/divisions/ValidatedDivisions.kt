package com.github.plplmax.planning.divisions

class ValidatedDivisions(private val origin: Divisions) : Divisions by origin {
    override suspend fun insert(division: NewDivision): DivisionDetail {
        validateSubgroups(division.subgroups.map(::preprocessSubgroup))
        return origin.insert(division)
    }

    override suspend fun update(division: DivisionDetail): DivisionDetail {
        validateSubgroups(division.subgroups.map(::preprocessSubgroup))
        return origin.update(division)
    }

    private fun validateSubgroups(subgroups: List<Subgroup>) {
        check(subgroups.isNotEmpty()) { "Division should have at least one subgroup" }
        subgroups.forEach { validateSubgroupName(it.name) }
    }

    private fun validateSubgroupName(name: String) {
        check(name.length in 1..64) { "Subgroup name should be from 1 to 64 characters" }
    }

    private fun preprocessSubgroup(subgroup: Subgroup): Subgroup = subgroup.copy(name = subgroup.name.trim())
}
