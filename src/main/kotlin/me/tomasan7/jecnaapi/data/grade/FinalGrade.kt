package me.tomasan7.jecnaapi.data.grade

data class FinalGrade(val value: Int,
                      val subject: String? = null)
{
    init
    {
        require(value in 1..5) { "Grade value must be between 1 and 5. (got $value)" }
    }
}