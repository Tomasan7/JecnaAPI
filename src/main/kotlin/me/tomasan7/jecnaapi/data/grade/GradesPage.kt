package me.tomasan7.jecnaapi.data.grade

import java.util.*

/**
 * Representing grades table.
 * Stores `0` or more grades for each subject.
 */
class GradesPage private constructor(private val grades: Map<String, Subject>)
{
    /** All subjects. */
    val subjects = grades.keys

    /**
     * @return [Subject] with the passed name. Can be `null`, when theres no subject with that name.
     */
    fun getSubjectByName(subject: String) = grades.getOrDefault(subject, null)

    /**
     * This [GradesPage] as a [Map].
     * Key = subject name, value = [Subject].
     */
    val asMap = grades

    class Builder
    {
        private val grades: MutableMap<String, Subject> = HashMap()

        /**
         * Adds [Subject].
         * @param subject The subject to add.
         * @return This [Builder] instance back.
         */
        fun addSubject(subject: Subject): Builder
        {
            grades[subject.name] = subject
            return this
        }

        fun build() = GradesPage(grades)
    }

    companion object
    {
        fun builder() = Builder()
    }
}