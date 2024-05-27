package com.github.plplmax.planning.subjects.paired

import com.github.plplmax.planning.subjects.Subject

interface PairedSubjectsCollection {
    suspend fun all(): List<PairedSubjects>
    suspend fun allBySubjects(
        firstSubject: Subject,
        secondSubject: Subject,
        excludedIds: List<Int> = listOf()
    ): List<PairedSubjects>
    suspend fun insert(subjects: NewPairedSubjects): PairedSubjects
    suspend fun update(subjects: PairedSubjects): PairedSubjects
    suspend fun delete(id: Int): Int
}
