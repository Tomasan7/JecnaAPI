package me.tomasan7.jecnaapi.data.timetable

import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Represents a [Lesson]'s time period in a timetable.
 * @param from Lesson's starting time.
 * @param to Lesson's ending time.
 */
data class LessonPeriod(val from: LocalTime, val to: LocalTime)
{
    override fun toString(): String
    {
        return from.format(formatter) + " - " + to.format(formatter)
    }

    companion object
    {
        private val formatter = DateTimeFormatter.ofPattern("H:mm")

        /**
         * Parses [LessonPeriod] from [String]. **The [String] must be in a "HH:mm - HH:mm" format.**
         * @param string The [String] to parse from.
         * @throws IllegalArgumentException When the provided [String] is in incorrect format.
         * @return The parsed [LessonPeriod].
         */
        fun fromString(string: String): LessonPeriod
        {
            val split = string.split(" - ")

            try
            {
                return LessonPeriod(LocalTime.parse(split[0], formatter),
                                    LocalTime.parse(split[1], formatter))
            }
            catch (e: IndexOutOfBoundsException)
            {
                throw IllegalArgumentException("Provided string wasn't in correct format. Expected format \"HH:mm - HH:mm\", got \"$string\".")
            }
        }
    }
}