package me.tomasan7.jecnaapi.data.timetable

import kotlinx.serialization.Serializable
import me.tomasan7.jecnaapi.util.Name

/**
 * A lesson in a timetable.
 *
 * @param group The group's number. Will be `0`, when there aren't groups.
 */
@Serializable
data class Lesson(
    val subjectName: Name,
    val clazz: String? = null,
    val teacherName: Name?,
    val classroom: String?,
    val group: String?
)