package me.tomasan7.jecnaapi.util

import java.time.LocalDate
import java.time.Month

data class SchoolYear(val firstCalendarYear: Int): Comparable<SchoolYear>
{
    val secondCalendarYear: Int = firstCalendarYear + 1

    /**
     * Constructs a [SchoolYear] this [date] belongs to. Considers the summer holidays as a new year.
     */
    constructor(date: LocalDate) : this(if (date.month in FIRST_CALENDAR_YEAR_MONTHS) date.year else date.year - 1)

    /**
     * Returns calendar year [month] is in based on this [SchoolYear]. Considers the summer holidays as a new year.
     */
    fun getCalendarYear(month: Month): Int
    {
        return if (month in FIRST_CALENDAR_YEAR_MONTHS)
            firstCalendarYear
        else
            secondCalendarYear
    }

    /**
     * @return Whether the passed date is inside this [SchoolYear]. Considers the summer holidays as a new year.
     */
    operator fun contains(date: LocalDate): Boolean
    {
        return (date.year == firstCalendarYear
                && date.month in FIRST_CALENDAR_YEAR_MONTHS)
               ||
               (date.year == secondCalendarYear
                && date.month in SECOND_CALENDAR_YEAR_MONTHS)
    }

    override fun compareTo(other: SchoolYear) = firstCalendarYear.compareTo(other.firstCalendarYear)

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
        /**
         * All [months][Month] in the first calendar year of a [SchoolYear]. (with summer holidays)
         */
        private val FIRST_CALENDAR_YEAR_MONTHS = Month.JULY..Month.DECEMBER

        /**
         * All [months][Month] represented by their [Month.getValue] in the first calendar year of a [SchoolYear]. (with summer holidays)
         */
        private val FIRST_CALENDAR_YEAR_MONTHS_VALUES = FIRST_CALENDAR_YEAR_MONTHS.mapToIntRange { it.value }

        /**
         * All [months][Month] in the second calendar year of a [SchoolYear]. (without summer holidays)
         */
        private val SECOND_CALENDAR_YEAR_MONTHS = Month.JANUARY..Month.JUNE

        /**
         * All [months][Month] represented by their [Month.getValue] in the second calendar year of a [SchoolYear]. (without summer holidays)
         */
        private val SECOND_CALENDAR_YEAR_MONTHS_VALUES = SECOND_CALENDAR_YEAR_MONTHS.mapToIntRange { it.value }

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