package me.tomasan7.jecnaapi.data.grade

import me.tomasan7.jecnaapi.util.Name
import me.tomasan7.jecnaapi.util.toName
import java.util.*

/**
 * Representing grades table.
 * Stores `0` or more grades for each subject.
 */
class GradesPage private constructor(private val grades: Map<Name, Subject>,
                                     val behaviour: Behaviour)
{
    /** All subject names. */
    val subjectNames = grades.keys

    /**
     * @return [Subject] with the passed [Name]. Can be `null`, when theres no subject with that name.
     */
    fun getSubjectByName(subjectName: Name) = grades.getOrDefault(subjectName, null)

    /**
     * @return [Subject] with the passed [subjectName] as it's full name. Can be `null`, when theres no subject with that name.
     */
    fun getSubjectByName(subjectName: String) = grades.getOrDefault(subjectName.toName(), null)

    /**
     * This [GradesPage] as a [Map].
     * Key = subject name, value = [Subject].
     */
    val asMap = grades

    class Builder
    {
        private val grades: MutableMap<Name, Subject> = HashMap()
        private lateinit var behaviour: Behaviour

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

        fun setBehaviour(behaviour: Behaviour)
        {
            this.behaviour = behaviour
        }

        fun build(): GradesPage
        {
            check(::behaviour.isInitialized) { "Behaviour has not been set." }
            return GradesPage(grades, behaviour)
        }
    }

    companion object
    {
        fun builder() = Builder()
    }
}