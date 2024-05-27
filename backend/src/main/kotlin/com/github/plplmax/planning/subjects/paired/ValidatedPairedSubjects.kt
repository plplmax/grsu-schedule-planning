package com.github.plplmax.planning.subjects.paired

import com.github.plplmax.planning.subjects.Subject

class ValidatedPairedSubjects(private val origin: PairedSubjectsCollection) : PairedSubjectsCollection by origin {
    override suspend fun insert(subjects: NewPairedSubjects): PairedSubjects {
        validateSubjectsNotEqual(subjects.firstSubject, subjects.secondSubject)
        validateCount(subjects.count)
        origin.allBySubjects(subjects.firstSubject, subjects.secondSubject) +
                origin.allBySubjects(subjects.secondSubject, subjects.firstSubject)
                    .flatMap(PairedSubjects::groups)
                    .let { groups -> subjects.groups.any { groups.contains(it) } }
                    .let {
                        if (it) error(
                            ERROR_MESSAGE.format(
                                subjects.firstSubject.name,
                                subjects.secondSubject.name,
                                subjects.secondSubject.name,
                                subjects.firstSubject.name
                            )
                        )
                    }

        return origin.insert(subjects)
    }

    override suspend fun update(subjects: PairedSubjects): PairedSubjects {
        validateSubjectsNotEqual(subjects.firstSubject, subjects.secondSubject)
        validateCount(subjects.count)
        origin.allBySubjects(subjects.firstSubject, subjects.secondSubject, listOf(subjects.id)) +
                origin.allBySubjects(subjects.secondSubject, subjects.firstSubject, listOf(subjects.id))
                    .flatMap(PairedSubjects::groups)
                    .let { groups -> subjects.groups.any { groups.contains(it) } }
                    .let {
                        if (it) error(
                            ERROR_MESSAGE.format(
                                subjects.firstSubject.name,
                                subjects.secondSubject.name,
                                subjects.secondSubject.name,
                                subjects.firstSubject.name
                            )
                        )
                    }

        return origin.update(subjects)
    }

    private fun validateCount(count: Int) {
        check(count in 1..5) { error("Count should be in range 1..5") }
    }

    private fun validateSubjectsNotEqual(first: Subject, second: Subject) {
        check(first.id != second.id) { error("Equal first and second subjects are prohibited") }
    }

    companion object {
        private const val ERROR_MESSAGE =
            "There is minimum one group that already exists in paired subjects (%s, %s) or (%s, %s)"
    }
}
