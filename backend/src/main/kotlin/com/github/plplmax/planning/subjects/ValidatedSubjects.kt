package com.github.plplmax.planning.subjects

class ValidatedSubjects(private val origin: Subjects) : Subjects by origin {
    override suspend fun insert(subject: NewSubject): Subject {
        val processedSubject = subject.copy(name = processName(subject.name))
        validateName(processedSubject.name)
        return origin.insert(processedSubject)
    }

    override suspend fun update(subject: Subject): Subject {
        val processedSubject = subject.copy(name = processName(subject.name))
        validateName(processedSubject.name)
        return origin.update(processedSubject)
    }

    private fun processName(name: String): String {
        return name.trim().lowercase().replaceFirstChar { it.uppercaseChar() }
    }

    private suspend fun validateName(name: String) {
        check(name.length in 1..64) { "Subject name should be from 1 to 64 characters" }
        check(!origin.all().any { it.name == name }) { "Subject with `name` = $name already exists}" }
    }
}
