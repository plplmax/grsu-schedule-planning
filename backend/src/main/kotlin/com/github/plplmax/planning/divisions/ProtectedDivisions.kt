package com.github.plplmax.planning.divisions

class ProtectedDivisions(private val origin: Divisions) : Divisions by origin {
    override suspend fun update(division: DivisionDetail): DivisionDetail {
        checkRestrictions(division.id)
        return origin.update(division)
    }

    override suspend fun delete(id: Int): Int {
        checkRestrictions(id)
        return origin.delete(id)
    }

    private fun checkRestrictions(id: Int) {
        check(id != SYSTEM_DIVISION_ID) { "System division with id = $SYSTEM_DIVISION_ID cannot be modified or deleted" }
    }

    companion object {
        private const val SYSTEM_DIVISION_ID = 1
    }
}
