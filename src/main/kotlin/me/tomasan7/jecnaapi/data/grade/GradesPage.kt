package me.tomasan7.jecnaapi.data.grade

import java.util.*
import kotlin.collections.HashMap

/**
 * Representing grades table.
 * Stores `0` or more grades for each subject.
 */
class GradesPage private constructor(private val grades: Map<String, List<Grade>>)
{
    /** All subjects. */
    val subjects = grades.keys

    /**
     * @return List of all [grades][Grade] for [subject]. Can be empty, if no grades are present for that subject.
     */
    fun getGradesForSubject(subject: String) = grades.getOrDefault(subject, listOf())

    /**
     * This [GradesPage] as a [Map].
     * Key = subject, value = list of [grades][Grade].
     */
    val asMap = grades

    class Builder
    {
        private val grades: MutableMap<String, MutableList<Grade>> = HashMap()

        /**
         * Adds [Grade].
         * @param subject The subject to add this grade to. **Must be the same format is in the grades table rows.**
         * @param grade The [Grade] to add.
         * @return This [builder&#39;s][Builder] instance back.
         */
        fun addGrade(subject: String, grade: Grade): Builder
        {
            /* Gets the list for the subject, if none is present, creates a new list and puts it into the map. Then the grade is added to that list. */
            grades.computeIfAbsent(subject) { LinkedList() }.add(grade)
            return this
        }

        fun build() = GradesPage(grades)
    }

    companion object
    {
        fun builder() = Builder()
    }
}