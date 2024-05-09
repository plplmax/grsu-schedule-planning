package com.github.plplmax.planning.groups

class ValidatedGroups(private val origin: Groups) : Groups by origin {
    override suspend fun insert(group: NewGroup): GroupDetail {
        validateNumber(group.number)
        validateLetter(group.letter)
        return origin.insert(group)
    }

    override suspend fun update(group: GroupDetail): GroupDetail {
        validateNumber(group.number)
        validateLetter(group.letter)
        return origin.update(group)
    }

    private fun validateNumber(number: Int) {
        check(number in 1..11) { "Group number should be from 1 to 11" }
    }

    private fun validateLetter(letter: Char) {
        val letters = listOf('А', 'Б', 'В', 'Г', 'Д', 'Е', 'Ж', 'З', 'И', 'К')
        check(letter in letters) { "Group letter should be one of the following letters: $letters" }
    }
}
