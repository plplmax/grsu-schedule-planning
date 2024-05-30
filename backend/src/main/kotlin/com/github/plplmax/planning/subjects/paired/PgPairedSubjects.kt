package com.github.plplmax.planning.subjects.paired

import com.github.plplmax.planning.database.tables.GroupsTable
import com.github.plplmax.planning.database.tables.GroupsToPairedSubjectsTable
import com.github.plplmax.planning.database.tables.PairedSubjectsTable
import com.github.plplmax.planning.database.tables.SubjectsTable
import com.github.plplmax.planning.groups.Group
import com.github.plplmax.planning.groups.PgGroups
import com.github.plplmax.planning.subjects.Subject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class PgPairedSubjects(
    private val database: Database,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : PairedSubjectsCollection {
    override suspend fun all(): List<PairedSubjects> {
        return newSuspendedTransaction(dispatcher, database) {
            PairedSubjectsTable.innerJoin(firstSubject, { firstSubjectId }, { firstSubject[SubjectsTable.id] })
                .innerJoin(secondSubject, { PairedSubjectsTable.secondSubjectId }, { secondSubject[SubjectsTable.id] })
                .leftJoin(GroupsToPairedSubjectsTable)
                .leftJoin(GroupsTable)
                .selectAll()
                .orderBy(
                    firstSubject[SubjectsTable.name] to SortOrder.ASC,
                    secondSubject[SubjectsTable.name] to SortOrder.ASC,
                    PairedSubjectsTable.id to SortOrder.ASC
                )
                .let(::toPairedSubjects)
        }
    }

    override suspend fun allBySubjects(
        first: Subject,
        second: Subject,
        excludedIds: List<Int>
    ): List<PairedSubjects> {
        return newSuspendedTransaction(dispatcher, database) {
            PairedSubjectsTable.innerJoin(firstSubject, { firstSubjectId }, { firstSubject[SubjectsTable.id] })
                .innerJoin(secondSubject, { PairedSubjectsTable.secondSubjectId }, { secondSubject[SubjectsTable.id] })
                .leftJoin(GroupsToPairedSubjectsTable)
                .leftJoin(GroupsTable)
                .select {
                    (PairedSubjectsTable.id notInList excludedIds) and
                            (PairedSubjectsTable.firstSubjectId eq first.id) and
                            (PairedSubjectsTable.secondSubjectId eq second.id)
                }
                .orderBy(
                    firstSubject[SubjectsTable.name] to SortOrder.ASC,
                    secondSubject[SubjectsTable.name] to SortOrder.ASC,
                    PairedSubjectsTable.id to SortOrder.ASC
                )
                .let(::toPairedSubjects)
        }
    }

    override suspend fun insert(subjects: NewPairedSubjects): PairedSubjects {
        return newSuspendedTransaction(dispatcher, database) {
            val id = PairedSubjectsTable.insertAndGetId {
                it[firstSubjectId] = subjects.firstSubject.id
                it[secondSubjectId] = subjects.secondSubject.id
                it[count] = subjects.count
            }.value

            GroupsToPairedSubjectsTable.batchInsert(subjects.groups) {
                this[GroupsToPairedSubjectsTable.groupId] = it.id
                this[GroupsToPairedSubjectsTable.pairedSubjectsId] = id
            }

            PairedSubjectsTable.innerJoin(firstSubject, { firstSubjectId }, { firstSubject[SubjectsTable.id] })
                .innerJoin(secondSubject, { PairedSubjectsTable.secondSubjectId }, { secondSubject[SubjectsTable.id] })
                .leftJoin(GroupsToPairedSubjectsTable)
                .leftJoin(GroupsTable)
                .select { PairedSubjectsTable.id eq id }
                .orderBy(GroupsTable.number to SortOrder.DESC, GroupsTable.letter to SortOrder.ASC)
                .let(::toPairedSubjects)
                .firstOrNull() ?: error("There is no paired subjects with id = $id after inserting")
        }
    }

    override suspend fun update(subjects: PairedSubjects): PairedSubjects {
        return newSuspendedTransaction(dispatcher, database) {
            PairedSubjectsTable.update({ PairedSubjectsTable.id eq subjects.id }) {
                it[firstSubjectId] = subjects.firstSubject.id
                it[secondSubjectId] = subjects.secondSubject.id
                it[count] = subjects.count
            }.let {
                check(it > 0) { "There is no affected rows after updating paired subjects with id = ${subjects.id}" }
            }

            val groups = GroupsToPairedSubjectsTable.innerJoin(GroupsTable)
                .select { GroupsToPairedSubjectsTable.pairedSubjectsId eq subjects.id }
                .map(PgGroups::toGroup)

            val added = subjects.groups - groups.toSet()
            val deleted = groups - subjects.groups.toSet()

            GroupsToPairedSubjectsTable.batchInsert(added) {
                this[GroupsToPairedSubjectsTable.groupId] = it.id
                this[GroupsToPairedSubjectsTable.pairedSubjectsId] = subjects.id
            }

            GroupsToPairedSubjectsTable.deleteWhere {
                (pairedSubjectsId eq subjects.id) and (groupId inList deleted.map(
                    Group::id
                ))
            }.let {
                check(it == deleted.size) { "Expected deleted groups to paired subjects = ${deleted.size}, actual = $it" }
            }

            PairedSubjectsTable.innerJoin(firstSubject, { firstSubjectId }, { firstSubject[SubjectsTable.id] })
                .innerJoin(secondSubject, { PairedSubjectsTable.secondSubjectId }, { secondSubject[SubjectsTable.id] })
                .leftJoin(GroupsToPairedSubjectsTable)
                .leftJoin(GroupsTable)
                .select { PairedSubjectsTable.id eq subjects.id }
                .orderBy(GroupsTable.number to SortOrder.DESC, GroupsTable.letter to SortOrder.ASC)
                .let(::toPairedSubjects)
                .firstOrNull() ?: error("There is no paired subjects with id = ${subjects.id} after updating")
        }
    }

    override suspend fun delete(id: Int): Int {
        return newSuspendedTransaction(dispatcher, database) {
            PairedSubjectsTable.deleteWhere { PairedSubjectsTable.id eq id }
                .also {
                    check(it > 0) { "Database did not delete any paired subjects with id = $id" }
                }
        }
    }

    companion object {
        private val firstSubject = SubjectsTable.alias("firstSubject")
        private val secondSubject = SubjectsTable.alias("secondSubject")

        fun toPairedSubjects(query: Query): List<PairedSubjects> {
            val groups = mutableMapOf<Int, MutableList<Group>>()
            val pairedSubjects = mutableMapOf<Int, PairedSubjects>()

            query.forEach { row ->
                val pairedId = row[PairedSubjectsTable.id].value
                groups.computeIfAbsent(pairedId) { mutableListOf() }
                row.getOrNull(GroupsTable.id)?.let { groups.getValue(pairedId).add(PgGroups.toGroup(row)) }
                pairedSubjects.computeIfAbsent(pairedId) {
                    PairedSubjects(
                        id = it,
                        firstSubject = Subject(
                            row[firstSubject[SubjectsTable.id]].value,
                            row[firstSubject[SubjectsTable.name]],
                            row[firstSubject[SubjectsTable.complexity]],
                            row[firstSubject[SubjectsTable.minDaysBetween]],
                            row[firstSubject[SubjectsTable.minDaysStrict]]
                        ),
                        secondSubject = Subject(
                            row[secondSubject[SubjectsTable.id]].value,
                            row[secondSubject[SubjectsTable.name]],
                            row[secondSubject[SubjectsTable.complexity]],
                            row[secondSubject[SubjectsTable.minDaysBetween]],
                            row[secondSubject[SubjectsTable.minDaysStrict]]
                        ),
                        groups = listOf(),
                        count = row[PairedSubjectsTable.count]
                    )
                }
            }

            return pairedSubjects.values.map { it.copy(groups = groups.getValue(it.id)) }
        }
    }
}
