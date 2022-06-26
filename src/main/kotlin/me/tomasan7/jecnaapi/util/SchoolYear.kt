package me.tomasan7.jecnaapi.util

import java.time.LocalDate
import java.time.Month

data class SchoolYear(val firstCalendarYear: Int)
{
    val secondCalendarYear: Int = firstCalendarYear + 1

    /**
     * Constructs a [SchoolYear] this [date] belongs to. Considers the summer holidays as a new year.
     */
    constructor(date: LocalDate) : this(if (SCHOOL_YEAR_LAST_MONTH <= date.monthValue) date.year else date.year + 1)

    /**
     * Returns calendar year [month] is in based on this [SchoolYear]. Considers the summer holidays as a new year.
     */
    fun getCalendarYear(month: Month): Int
    {
        val monthValue = month.value

        return if (monthValue in 1..SCHOOL_YEAR_LAST_MONTH)
            secondCalendarYear
        else
            firstCalendarYear
    }

    /**
     * @return Whether the passed date is inside this [SchoolYear]. Considers the summer holidays as a new year.
     */
    operator fun contains(date: LocalDate): Boolean
    {
        return (date.year == firstCalendarYear
                && SCHOOL_YEAR_LAST_MONTH <= date.monthValue)
               ||
               (date.year == secondCalendarYear
                && date.monthValue <= SCHOOL_YEAR_LAST_MONTH)
    }

    operator fun compareTo(other: SchoolYear) = firstCalendarYear.compareTo(other.firstCalendarYear)

    operator fun rangeTo(other: SchoolYear) = (firstCalendarYear..other.firstCalendarYear).map { SchoolYear(it) }

    override fun equals(other: Any?): Boolean
    {
        other ?: return false

        if (other !is SchoolYear) return false

        return firstCalendarYear == other.firstCalendarYear
    }

    override fun hashCode() = firstCalendarYear

    override fun toString() = "$firstCalendarYear/$secondCalendarYear"

    companion object
    {
        /** Last month in a school year - June. */
        private const val SCHOOL_YEAR_LAST_MONTH = 6

        /** First month in a school year - August. */
        private const val SCHOOL_YEAR_FIRST_MONTH = 9

        /**
         * @return [SchoolYear] represented in [String]. The [string] **must be in `"firstYear/secondYear"` format.** (eg. 2021/2022)
         * @throws IllegalArgumentException When the [string] [String] is not in the correct format.
         * @see [SchoolYear.toString]
         */
        fun fromString(string: String): SchoolYear
        {
            val split = string.split("/")

            val firstYear: Int
            val secondYear: Int

            try
            {
                firstYear = split[0].toInt()
                secondYear = split[1].toInt()
            }
            catch (e: Exception)
            {
                throw IllegalArgumentException("SchoolYear String wasn't in correct format. (firstYear/secondYear)")
            }

            if (firstYear + 1 != secondYear)
                throw IllegalArgumentException("SchoolYear String wasn't in correct format. SecondYear must be equal to firstYear + 1.")

            return SchoolYear(firstYear)
        }
    }
}

/**
 * Creates [SchoolYear] from receiver [Int] as the first calendar year.
 * @receiver The first calendar year of the resulting [SchoolYear].
 */
fun Int.schoolYear() = SchoolYear(this)

/**
 * @return [SchoolYear] represented in [String]. The [String] **must be in `"firstYear/secondYear"` format.** (eg. 2021/2022)
 * @throws IllegalArgumentException When the [String] [String] is not in the correct format.
 * @see [SchoolYear.toString]
 */
fun String.toSchoolYear() = SchoolYear.fromString(this)

/**
 * Constructs a [SchoolYear] this [LocalDate] belongs to. Considers the summer holidays as a new year.
 */
fun LocalDate.schoolYear() = SchoolYear(this)