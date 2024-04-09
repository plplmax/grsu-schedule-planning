package com.github.plplmax.planning.teachers

class ValidatedTeachers(private val origin: Teachers) : Teachers by origin {
    override suspend fun insert(teacher: NewTeacher): Teacher {
        val processedTeacher =
            teacher.copy(firstname = processName(teacher.firstname), lastname = processName(teacher.lastname))
        validateNameLength(teacher.firstname, teacher.lastname)
        check(
            !origin.exists(teacher.firstname, teacher.lastname)
        ) { UNIQUE_NAME_ERROR.format(teacher.firstname, teacher.lastname) }
        return origin.insert(processedTeacher)
    }

    override suspend fun update(teacher: Teacher): Teacher {
        val processedTeacher =
            teacher.copy(firstname = processName(teacher.firstname), lastname = processName(teacher.lastname))
        validateNameLength(teacher.firstname, teacher.lastname)
        check(
            !origin.exists(teacher.firstname, teacher.lastname, listOf(teacher.id))
        ) { UNIQUE_NAME_ERROR.format(teacher.firstname, teacher.lastname) }
        return origin.update(processedTeacher)
    }

    private fun processName(name: String): String {
        return name.trim().lowercase().replaceFirstChar { it.uppercaseChar() }
    }

    private fun validateNameLength(firstname: String, lastname: String) {
        check(firstname.length in 1..32) { "First name should be from 1 to 32 characters" }
        check(lastname.length in 1..32) { "Last name should be from 1 to 32 characters" }
    }

    companion object {
        private const val UNIQUE_NAME_ERROR =
            "Teacher with `firstname` = %s and `lastname` = %s already exists"
    }
}
