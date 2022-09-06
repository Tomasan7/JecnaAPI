package me.tomasan7.jecnaapi.data.timetable

/**
 * Represents a spot for [lessons][Lesson] in a timetable.
 * That spot can contain multiple [lessons][Lesson].
 * For example some lessons are split into more groups.
 * This class indicates the one whole lesson and contains the lessons for each group.
 */
class LessonSpot(lessons: List<Lesson>) : Iterable<Lesson>
{
    private val lessons: List<Lesson>

    /**
     * The number of lessons in this [LessonSpot].
     */
    val size: Int
        get() = lessons.size

    constructor(lesson: Lesson) : this(listOf(lesson))

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