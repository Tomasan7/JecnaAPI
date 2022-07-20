package me.tomasan7.jecnaapi.data

import me.tomasan7.jecnaapi.data.LessonSpot
import me.tomasan7.jecnaapi.util.emptyMutableLinkedList
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList

/**
 * Whole timetable containing [LessonSpot]s for each day and their [LessonPeriod]s.
 */
class TimetablePage private constructor(private val timetable: Map<String, List<LessonSpot?>>,
                                        val lessonPeriods: List<LessonPeriod>)
{
    /**
     * @return All the days this timetable has [lessons][Lesson] in.
     */
    val days = timetable.keys

    /**
     * @return All the days this timetable has [lessons][Lesson] in. Sorted as it goes in a week.
     */
    val daysSorted = timetable.keys.sortedWith(DayComparator())

    /**
     * Returns all [lessons][LessonSpot] for the provided day.
     * The [lessons][LessonSpot] lessons are ordered by the hour they are in.
     *
     * @param day The day to get all [lessons][Lesson] for.
     * @return All [lessons][LessonSpot] for the provided day.
     */
    fun getLessonsForDay(day: String) = timetable.getOrDefault(day, emptyList())

    override fun toString(): String
    {
        return "Timetable{" +
               "timetable=" + timetable +
               ", lessonHours=" + lessonPeriods +
               '}'
    }

    companion object
    {
        fun builder() = Builder()
    }

    class Builder
    {
        private val timetable: MutableMap<String, MutableList<LessonSpot?>> = TreeMap(DayComparator())
        private var lessonPeriods: MutableList<LessonPeriod> = ArrayList()

        /**
         * Sets all the [LessonPeriods][LessonPeriod].
         *
         * @param lessonPeriods The [LessonPeriods][LessonPeriod] to use.
         * @return This [builder's][Builder] instance back.
         */
        fun lessonHours(lessonPeriods: MutableList<LessonPeriod>): Builder
        {
            this.lessonPeriods = lessonPeriods
            return this
        }

        /**
         * Sets a [LessonPeriod] to a specified hour index.
         *
         * @param hour       The hour index to set the [LessonPeriod] to.
         * @param lessonPeriod The [LessonPeriod] to set.
         * @return This [builder's][Builder] instance back.
         */
        fun setLessonPeriod(hour: Int, lessonPeriod: LessonPeriod): Builder
        {
            lessonPeriods[hour] = lessonPeriod
            return this
        }

        /**
         * Adds a [LessonPeriod] to the [TimetablePage].
         *
         * @param lessonPeriod The [LessonPeriod] to add.
         * @return This [builder's][Builder] instance back.
         */
        fun addLessonPeriod(lessonPeriod: LessonPeriod): Builder
        {
            lessonPeriods.add(lessonPeriod)
            return this
        }

        /**
         * Sets a [LessonSpot] to an hour in the provided day.
         * Can be `null`, if there is no lesson at that time.
         * Overrides any existing [LessonSpots][LessonSpot].
         *
         * @param day    The day to set the [LessonSpot] to.
         * @param hour   The hour to set the [LessonSpot] to.
         * @param lessonSpot The [LessonSpot] to be set.
         * @return This [builder's][Builder] instance back.
         */
        fun setLessonSpot(day: String, hour: Int, lessonSpot: LessonSpot?): Builder
        {
            /* Gets the list for the day, if none is present, creates a new list and puts it into the map. Then the lesson is added to that list. */
            timetable.computeIfAbsent(day) { emptyMutableLinkedList() }[hour] = lessonSpot
            return this
        }

        /**
         * Shorthand for `setLessonSpot(day, hour, new LessonSpot(lesson))`.
         * @see .setLessonSpot
         * @see LessonSpot.LessonSpot
         */
        fun setLesson(day: String, hour: Int, lesson: Lesson?): Builder
        {
            /* Gets the list for the day, if none is present, creates a new list and puts it into the map. Then the lesson is added to that list. */
            timetable.computeIfAbsent(day) { emptyMutableLinkedList() }[hour] = lesson?.let { LessonSpot(it) }
            return this
        }

        /**
         * Adds a [LessonSpot] to a day.
         * Can be `null`, if there is no lesson at that time.
         * The [LessonSpot] gets appended to the end.
         *
         * @param day    The day to add the [LessonSpot] to.
         * @param lessonSpot The [LessonSpot] to add.
         * @return This [builder's][Builder] instance back.
         */
        fun addLessonSpot(day: String, lessonSpot: LessonSpot?): Builder
        {
            /* Gets the list for the day, if none is present, creates a new list and puts it into the map. Then the lesson is added to that list. */
            timetable.computeIfAbsent(day) { emptyMutableLinkedList() }.add(lessonSpot)
            return this
        }

        /**
         * Shorthand for `addLessonSpot(day, hour, new LessonSpot(lesson))`.
         * @see .setLessonSpot
         * @see LessonSpot.LessonSpot
         */
        fun addLesson(day: String, lesson: Lesson?): Builder
        {
            /* Gets the list for the day, if none is present, creates a new list and puts it into the map. Then the lesson is added to that list. */
            timetable.computeIfAbsent(day) { emptyMutableLinkedList() }
                .add(lesson?.let { LessonSpot(it) })
            return this
        }

        fun build(): TimetablePage
        {
            /* TODO: Maybe check if there is equal or more lessonPeriods than lessons in any day?
			 * Because that would mean there is a lesson without specified LessonPeriod. */
            return TimetablePage(timetable, lessonPeriods)
        }
    }

    /**
     * Compares two days based on their position in a week. Meaning tuesday < friday.
     * The **days must follow this format**: Po/Út/St/Čt/Pa/So/Ne.
     * **Note the "Pa", it is not "Pá".**
     */
    class DayComparator : Comparator<String>
    {

        override fun compare(s1: String, s2: String): Int
        {
            /* This method works by finding each day's position in a week using DAYS list and then subtracting their position. */

            if (s1.equals(s2, true))
                return 0

            var s1Pos = -1
            var s2Pos = -1

            for (i in DAYS.indices)
            {
                if (s1.equals(DAYS[i], true))
                {
                    s1Pos = i
                    continue
                }

                if (s2.equals(DAYS[i], true))
                    s2Pos = i

                if (s1Pos != -1 && s2Pos != -1)
                    break
            }

            return s1Pos - s2Pos
        }

        companion object
        {
            val DAYS = listOf("Po", "Út", "St", "Čt", "Pa", "So", "Ne")
        }
    }
}