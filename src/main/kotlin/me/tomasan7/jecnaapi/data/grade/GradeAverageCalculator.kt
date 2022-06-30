package me.tomasan7.jecnaapi.data.grade

/**
 * Calculates [Grades] average.
 * Has [calculate] function.
 */
interface GradeAverageCalculator
{
    /**
     * Calculates [Grades] average.
     *
     * @param grades The [Grades] to calculate the average from.
     * @return The calculated average grade as [Float].
     */
    fun calculate(grades: Grades): Float

    operator fun invoke(grades: Grades) = calculate(grades)
}