package me.tomasan7.jecnaapi.data.grade

import kotlinx.serialization.Serializable
import me.tomasan7.jecnaapi.serialization.NameSerializer
import me.tomasan7.jecnaapi.util.Name

@Serializable
data class Subject(
    @Serializable(with = NameSerializer::class)
    val name: Name,
    val grades: Grades,
    val finalGrade: FinalGrade? = null
)