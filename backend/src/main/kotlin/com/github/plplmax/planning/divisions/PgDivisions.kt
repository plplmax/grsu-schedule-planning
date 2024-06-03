package com.github.plplmax.planning.divisions

import com.github.plplmax.planning.database.tables.DivisionsTable
import com.github.plplmax.planning.database.tables.SubgroupsTable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class PgDivisions(
    private val database: Database,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : Divisions {
    override suspend fun all(): List<DivisionDetail> {
        return newSuspendedTransaction(dispatcher, database) {
            DivisionsTable.innerJoin(SubgroupsTable)
                .selectAll()
                .orderBy(DivisionsTable.id to SortOrder.ASC, SubgroupsTable.id to SortOrder.ASC)
                .let(::toDivisionDetails)
        }
    }

    override suspend fun allSubgroups(): List<SubgroupDetail> {
        return newSuspendedTransaction(dispatcher, database) {
            DivisionsTable.innerJoin(SubgroupsTable)
                .selectAll()
                .orderBy(DivisionsTable.id to SortOrder.ASC, SubgroupsTable.id to SortOrder.ASC)
                .map(::toSubgroupDetail)
        }
    }

    override suspend fun insert(division: NewDivision): DivisionDetail {
        return newSuspendedTransaction(dispatcher, database) {
            val id = DivisionsTable.insertAndGetId {}.value

            SubgroupsTable.batchInsert(division.subgroups) {
                this[SubgroupsTable.name] = it.name
                this[SubgroupsTable.divisionId] = id
            }

            DivisionsTable.innerJoin(SubgroupsTable)
                .select { DivisionsTable.id eq id }
                .orderBy(DivisionsTable.id to SortOrder.ASC, SubgroupsTable.id to SortOrder.ASC)
                .let(::toDivisionDetails)
                .firstOrNull() ?: error("There is no result after inserting a new division")
        }
    }

    override suspend fun update(division: DivisionDetail): DivisionDetail {
        return newSuspendedTransaction(dispatcher, database) {
            division.subgroups.forEach { subgroup ->
                SubgroupsTable.update({ SubgroupsTable.id eq subgroup.id }) {
                    it[name] = subgroup.name
                }
            }

            val subgroups = SubgroupsTable.select { SubgroupsTable.divisionId eq division.id }
                .map(::toSubgroup)

            val added = division.subgroups - subgroups.toSet()
            val deleted = subgroups - division.subgroups.toSet()

            SubgroupsTable.batchInsert(added) {
                this[SubgroupsTable.name] = it.name
                this[SubgroupsTable.divisionId] = division.id
            }

            SubgroupsTable.deleteWhere { SubgroupsTable.id inList deleted.map(Subgroup::id) }

            DivisionsTable.innerJoin(SubgroupsTable)
                .select { DivisionsTable.id eq division.id }
                .orderBy(SubgroupsTable.id to SortOrder.ASC)
                .let(::toDivisionDetails)
                .firstOrNull() ?: error("There is no result after updating division")
        }
    }

    override suspend fun delete(id: Int): Int {
        return newSuspendedTransaction(dispatcher, database) {
            DivisionsTable.deleteWhere { DivisionsTable.id eq id }
                .also {
                    check(it > 0) { "Database did not delete any divisions with id = $id" }
                }
        }
    }

    companion object {
        fun toSubgroup(row: ResultRow): Subgroup = Subgroup(
            id = row[SubgroupsTable.id].value,
            name = row[SubgroupsTable.name]
        )

        fun toSubgroupDetail(row: ResultRow): SubgroupDetail = SubgroupDetail(
            id = row[SubgroupsTable.id].value,
            name = row[SubgroupsTable.name],
            division = Division(id = row[DivisionsTable.id].value)
        )

        fun toDivisionDetails(query: Query): List<DivisionDetail> {
            val divisions = mutableMapOf<Division, List<Subgroup>>()

            query.forEach { row ->
                val division = Division(id = row[DivisionsTable.id].value)
                val subgroup = Subgroup(id = row[SubgroupsTable.id].value, name = row[SubgroupsTable.name])
                divisions.merge(division, listOf(subgroup)) { a, b -> a + b }
            }

            return divisions.map { DivisionDetail(id = it.key.id, subgroups = it.value) }
        }
    }
}
