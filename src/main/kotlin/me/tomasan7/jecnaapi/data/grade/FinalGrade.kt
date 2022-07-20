package me.tomasan7.jecnaapi.data.grade

import me.tomasan7.jecnaapi.util.Name

data class FinalGrade(
    val value: Int,
    val subject: Name? = null
)
{
    init
    {
        require(value in 1..5) { "Grade value must be between 1 and 5. (got $value)" }
    }
}