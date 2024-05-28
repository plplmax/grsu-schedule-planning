package com.github.plplmax.planning.subjects

class ValidatedSubjects(private val origin: Subjects) : Subjects by origin {
    override suspend fun insert(subject: NewSubject): SubjectDetail {
        val processedSubject = subject.copy(name = processName(subject.name))
        validateName(processedSubject.name)
        validateComplexity(subject.complexity)
        return origin.insert(processedSubject)
    }

    override suspend fun update(subject: SubjectDetail): SubjectDetail {
        val processedSubject = subject.copy(name = processName(subject.name))
        validateName(processedSubject.name)
        validateComplexity(subject.complexity)
        return origin.update(processedSubject)
    }

    private fun processName(name: String): String {
        return name.trim().lowercase().replaceFirstChar { it.uppercaseChar() }
    }

    private fun validateName(name: String) {
        check(name.length in 1..64) { "Subject name should be from 1 to 64 characters" }
    }

    private fun validateComplexity(complexity: Int) {
        check(complexity in 0..12)
    }
}
