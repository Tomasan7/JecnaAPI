package me.tomasan7.jecnaapi.data.grade

import me.tomasan7.jecnaapi.util.Name

data class Subject(
    val name: Name,
    val grades: Grades,
    val finalGrade: FinalGrade? = null
)