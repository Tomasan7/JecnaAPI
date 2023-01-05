package me.tomasan7.jecnaapi.data.grade

import me.tomasan7.jecnaapi.util.Name

/**
 * Final grade, which is shown for each subject at the end of the school year half.
 */
sealed interface FinalGrade
{
    /**
     * A closed final grade.
     */
    data class Grade(
        val value: Int,
        val subject: Name? = null
    ) : FinalGrade
    {
        init
        {
            require(value in 1..5) { "Grade value must be between 1 and 5. (got $value)" }
        }
    }

    /**
     * Warning, when student has bad grades.
     */
    object GradesWarning : FinalGrade

    /**
     * Warning, when student doesn't have enough grades to be qualified.
     * Happens, when he is absent on tests.
     */
    object AbsenceWarning : FinalGrade

    /**
     * Student has both [GradesWarning] and [AbsenceWarning] at once.
     */
    object GradesAndAbsenceWarning : FinalGrade
}