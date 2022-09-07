package me.tomasan7.jecnaapi.data.timetable

import java.time.DayOfWeek

/**
 * Holds all information about a single spot in a [Timetable].
 * Includes it's [DayOfWeek], [LessonPeriod] and the [LessonSpot].
 */
data class TimetableSpot(
    val day: DayOfWeek,
    val lessonPeriod: LessonPeriod,
    val lessonSpot: LessonSpot
)
{
    /**
     * Whether there are no [lessons][Lesson] in this [spot][LessonSpot] or not.
     */
    fun isEmpty() = lessonSpot.isEmpty()
}