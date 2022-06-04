package me.tomasan7.jecnaapi.data

/**
 * A lesson in a timetable.
 *
 * @param group The group's number. Will be {@code 0}, when there aren't groups.
 */
data class Lesson(val subject: String,
                  val subjectShort: String,
                  val teacher: String,
                  val teacherShort: String,
                  val classroom: String,
                  val group: Int)