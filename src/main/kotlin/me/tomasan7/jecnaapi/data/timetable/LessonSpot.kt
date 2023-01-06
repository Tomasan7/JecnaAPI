package me.tomasan7.jecnaapi.data.timetable

import me.tomasan7.jecnaapi.util.hasDuplicate

/**
 * Represents a spot for [lessons][Lesson] in a timetable.
 * That spot can contain multiple [lessons][Lesson].
 * For example some lessons are split into more groups.
 * This class indicates the one whole lesson and contains the lessons for each group.
 *
 * @property periodSpan The number of [periods][LessonPeriod] this lesson spot spans over.
 */
class LessonSpot(lessons: List<Lesson>, val periodSpan: Int) : Iterable<Lesson>
{
    constructor(lesson: Lesson, periodSpan: Int) : this(listOf(lesson), periodSpan)

    constructor(lesson: Lesson) : this(listOf(lesson), 1)

    private val lessons: List<Lesson>

    /** The number of lessons in this [LessonSpot]. */
    val size: Int
        get() = lessons.size

    init
    {
        require(!hasDuplicateGroups(lessons)) { "Lessons cannot have duplicate groups." }

        this.lessons = lessons.sortedBy { it.group }
    }

    /** Returns whether there are any duplicate groups in provided [lessons][Lesson]. */
    private fun hasDuplicateGroups(lessons: Iterable<Lesson>) = lessons.hasDuplicate { it.group }

    /** Returns `true` if this [LessonSpot] contains no [lessons][Lesson]. */
    fun isEmpty() = size == 0

    /** Returns `true` if this [LessonSpot] contains at least one lesson. */
    fun isNotEmpty() = !isEmpty()

    /** Returns a [Lesson] with the specified [group], or `null` if there's no [Lesson] with that [group]. */
    fun getLessonByGroup(group: Int) = lessons.getOrNull(group)

    /** @return An [Iterator] of [Lesson], which doesn't modify this [LessonSpot]. */
    override fun iterator() = lessons.iterator()

    override fun toString(): String
    {
        return "LessonSpot{" +
               "lessons=" + lessons +
                ", periodSpan=" + periodSpan +
               '}'
    }

    companion object
    {
        /** Creates a [LessonSpot] with no [lessons][Lesson] and a [periodSpan] of `1`. */
        fun empty() = LessonSpot(emptyList(), 1)

        /** Creates a [LessonSpot] with no [lessons][Lesson] and provided [periodSpan]. */
        fun empty(periodSpan: Int) = LessonSpot(emptyList(), periodSpan)
    }
}