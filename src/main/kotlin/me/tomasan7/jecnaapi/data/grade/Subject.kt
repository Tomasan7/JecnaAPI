package me.tomasan7.jecnaapi.data.grade

data class Subject(val name: String,
                   val nameShort: String? = null,
                   val grades: List<Grade>,
                   val finalGrade: FinalGrade? = null)