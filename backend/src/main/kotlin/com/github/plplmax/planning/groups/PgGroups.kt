package com.github.plplmax.planning.groups

import com.github.plplmax.planning.database.tables.*
import com.github.plplmax.planning.lessons.Lesson
import com.github.plplmax.planning.rooms.PgRooms
import com.github.plplmax.planning.subjects.PgSubjects
import com.github.plplmax.planning.teachers.PgTeachers
import com.github.plplmax.planning.timeslots.PgTimeslots
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class PgGroups(
    private val database: Database,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : Groups {
    override suspend fun all(): List<GroupDetail> {
        return newSuspendedTransaction(dispatcher, database) {
            GroupsTable.leftJoin(LessonsTable)
                .leftJoin(TeachersTable, onColumn = { LessonsTable.teacherId }, otherColumn = { TeachersTable.id })
                .leftJoin(SubjectsTable, onColumn = { LessonsTable.subjectId }, otherColumn = { SubjectsTable.id })
                .leftJoin(RoomsTable)
                .leftJoin(TimeslotsTable)
                .selectAll()
                .orderBy(
                    GroupsTable.number to SortOrder.DESC,
                    GroupsTable.letter to SortOrder.ASC,
                    SubjectsTable.name to SortOrder.ASC
                )
                .let(::toGroupDetails)
        }
    }

    override suspend fun insert(group: NewGroup): GroupDetail {
        return newSuspendedTransaction(dispatcher, database) {
            val groupId = GroupsTable.insertAndGetId {
                it[number] = group.number
                it[letter] = group.letter
            }.value

            LessonsTable.batchInsert(group.lessons) {
                this[LessonsTable.groupId] = groupId
                this[LessonsTable.teacherId] = it.teacher.id
                this[LessonsTable.subjectId] = it.subject.id
                this[LessonsTable.roomId] = it.room.id
                this[LessonsTable.timeslotId] = it.timeslot?.id
            }

            GroupsTable.leftJoin(LessonsTable)
                .leftJoin(TeachersTable, onColumn = { LessonsTable.teacherId }, otherColumn = { TeachersTable.id })
                .leftJoin(SubjectsTable, onColumn = { LessonsTable.subjectId }, otherColumn = { SubjectsTable.id })
                .leftJoin(RoomsTable)
                .leftJoin(TimeslotsTable)
                .select { GroupsTable.id eq groupId }
                .orderBy(SubjectsTable.name to SortOrder.ASC)
                .let(::toGroupDetails)
                .firstOrNull() ?: error("There is no result after inserting a new group")
        }
    }

    override suspend fun update(group: GroupDetail): GroupDetail {
        return newSuspendedTransaction(dispatcher, database) {
            GroupsTable.update({ GroupsTable.id eq group.id }) {
                it[number] = group.number
                it[letter] = group.letter
            }.also { check(it > 0) { "There are no affected rows after updating group with id = ${group.id}" } }

            group.lessons.forEach { lesson ->
                LessonsTable.update({ (LessonsTable.groupId eq group.id) and (LessonsTable.id eq lesson.id) }) {
                    it[teacherId] = lesson.teacher.id
                    it[subjectId] = lesson.subject.id
                    it[roomId] = lesson.room.id
                    it[timeslotId] = lesson.timeslot?.id
                }
            }

            val dbGroups = GroupsTable.leftJoin(LessonsTable)
                .leftJoin(TeachersTable, onColumn = { LessonsTable.teacherId }, otherColumn = { TeachersTable.id })
                .leftJoin(SubjectsTable, onColumn = { LessonsTable.subjectId }, otherColumn = { SubjectsTable.id })
                .leftJoin(RoomsTable)
                .leftJoin(TimeslotsTable)
                .select { GroupsTable.id eq group.id }
                .let(::toGroupDetails)
                .flatMap { it.lessons }

            val added = group.lessons - dbGroups.toSet()
            val deleted = dbGroups - group.lessons.toSet()

            LessonsTable.batchInsert(added) {
                this[LessonsTable.groupId] = group.id
                this[LessonsTable.teacherId] = it.teacher.id
                this[LessonsTable.subjectId] = it.subject.id
                this[LessonsTable.roomId] = it.room.id
                this[LessonsTable.timeslotId] = it.timeslot?.id
            }

            LessonsTable.deleteWhere { LessonsTable.id inList deleted.map(Lesson::id) }

            GroupsTable.leftJoin(LessonsTable)
                .leftJoin(TeachersTable, onColumn = { LessonsTable.teacherId }, otherColumn = { TeachersTable.id })
                .leftJoin(SubjectsTable, onColumn = { LessonsTable.subjectId }, otherColumn = { SubjectsTable.id })
                .leftJoin(RoomsTable)
                .leftJoin(TimeslotsTable)
                .select { GroupsTable.id eq group.id }
                .orderBy(SubjectsTable.name to SortOrder.ASC)
                .let(::toGroupDetails)
                .firstOrNull() ?: error("There is no result after inserting a new group")
        }
    }

    override suspend fun delete(id: Int): Int {
        return newSuspendedTransaction(dispatcher, database) {
            GroupsTable.deleteWhere { GroupsTable.id eq id }
                .also {
                    check(it > 0) { "Database did not delete any groups with id = $id" }
                }
        }
    }

    companion object {
        fun toGroupDetails(query: Query): List<GroupDetail> {
            val groups = mutableMapOf<Group, List<Lesson>>()

            query.forEach { row ->
                val group = Group(
                    id = row[GroupsTable.id].value,
                    number = row[GroupsTable.number],
                    letter = row[GroupsTable.letter]
                )
                val lesson = row.getOrNull(LessonsTable.id)?.let {
                    Lesson(
                        id = it.value,
                        group = group,
                        teacher = PgTeachers.toTeacher(row),
                        subject = PgSubjects.toSubject(row),
                        room = PgRooms.toRoom(row),
                        timeslot = row.getOrNull(TimeslotsTable.id)?.let { PgTimeslots.toTimeslot(row) })
                }

                groups.merge(group, listOfNotNull(lesson)) { a, b -> a + b }
            }

            return groups.map {
                GroupDetail(
                    id = it.key.id,
                    number = it.key.number,
                    letter = it.key.letter,
                    lessons = it.value
                )
            }
        }
    }
}
