package com.github.plplmax.planning.lessons

import com.github.plplmax.planning.database.tables.*
import com.github.plplmax.planning.divisions.PgDivisions
import com.github.plplmax.planning.groups.PgGroups
import com.github.plplmax.planning.rooms.PgRooms
import com.github.plplmax.planning.subjects.PgSubjects
import com.github.plplmax.planning.teachers.PgTeachers
import com.github.plplmax.planning.timeslots.PgTimeslots
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class PgLessons(
    private val database: Database,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : Lessons {
    override suspend fun all(): List<Lesson> {
        return newSuspendedTransaction(dispatcher, database) {
            LessonsTable
                .innerJoin(GroupsTable)
                .innerJoin(SubgroupsTable)
                .innerJoin(DivisionsTable)
                .innerJoin(TeachersTable, onColumn = { LessonsTable.teacherId }, otherColumn = { TeachersTable.id })
                .innerJoin(SubjectsTable, onColumn = { LessonsTable.subjectId }, otherColumn = { SubjectsTable.id })
                .innerJoin(RoomsTable)
                .leftJoin(TimeslotsTable)
                .selectAll()
                .orderBy(
                    GroupsTable.number to SortOrder.DESC,
                    GroupsTable.letter to SortOrder.ASC,
                    SubjectsTable.name to SortOrder.ASC,
                    SubgroupsTable.id to SortOrder.ASC
                )
                .map(::toLesson)
        }
    }

    override suspend fun update(lessons: List<Lesson>) {
        return newSuspendedTransaction(dispatcher, database) {
            lessons.forEach { lesson ->
                LessonsTable.update({ (LessonsTable.id eq lesson.id) }) {
                    it[groupId] = lesson.group.id
                    it[subgroupId] = lesson.subgroup.id
                    it[teacherId] = lesson.teacher.id
                    it[subjectId] = lesson.subject.id
                    it[roomId] = lesson.room.id
                    it[timeslotId] = lesson.timeslot?.id
                }
            }
        }
    }

    companion object {
        fun toLesson(row: ResultRow): Lesson = Lesson(
            id = row[LessonsTable.id].value,
            group = PgGroups.toGroup(row),
            subgroup = PgDivisions.toSubgroupDetail(row),
            teacher = PgTeachers.toTeacher(row),
            subject = PgSubjects.toSubject(row),
            room = PgRooms.toRoom(row),
            timeslot = row.getOrNull(TimeslotsTable.id)?.let { PgTimeslots.toTimeslot(row) }
        )
    }
}
