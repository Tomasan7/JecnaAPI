package me.tomasan7.jecnaapi.data.timetable

import me.tomasan7.jecnaapi.util.Name

/**
 * A lesson in a timetable.
 *
 * @param group The group's number. Will be {@code 0}, when there aren't groups.
 * @param classroom I've once encountered a lesson without a classroom, so it seems that it doesn't have to always be present - hence nullable.
 */
data class Lesson(
    val subjectName: Name,
    val teacherName: Name,
    val classroom: String?,
    val group: Int
)