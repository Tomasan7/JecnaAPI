package me.tomasan7.jecnaapi.data.grade

import me.tomasan7.jecnaapi.data.Name

data class Subject(val name: Name,
                   val grades: List<Grade>,
                   val finalGrade: FinalGrade? = null)