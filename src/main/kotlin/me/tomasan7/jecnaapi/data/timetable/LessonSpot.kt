package me.tomasan7.jecnaapi.data.timetable

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

    /**
     * The number of lessons in this [LessonSpot].
     */
    val size: Int
        get() = lessons.size

    init
    {
        require(!findDuplicateGroups(lessons)) { "Lessons cannot have duplicate groups." }

        this.lessons = lessons.sortedBy { it.group }
    }

    /**
     * @return Whether there are any duplicate groups in provided [lessons][Lesson].
     */
    private fun findDuplicateGroups(lessons: Iterable<Lesson>): Boolean
    {
        val groups = mutableSetOf<Int>()

        for (lesson in lessons)
            if (!groups.add(lesson.group))
                return true

        return false
    }

    /**
     * @return Whether this [LessonSpot] contains any [lessons][Lesson] or not. `true` if not, `false` if yes.
     */
    fun isEmpty() = size == 0

    /**
     * @return A [Lesson] with the specified group. Or `null`, if there's no [Lesson] with that group.
     */
    fun getLessonByGroup(group: Int) = lessons.getOrNull(group)

    /**
     * @return An [Iterator] of [Lesson], which doesn't modify this [LessonSpot].
     */
    override fun iterator() = lessons.iterator()

    override fun toString(): String
    {
        return "LessonSpot{" +
               "lessons=" + lessons +
               '}'
    }

    companion object
    {
        /**
         * Creates a [LessonSpot] with no [lessons][Lesson].
         */
        fun empty() = LessonSpot(listOf())
    }
}