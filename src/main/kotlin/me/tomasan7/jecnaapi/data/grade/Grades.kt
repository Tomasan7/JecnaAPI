package me.tomasan7.jecnaapi.data.grade

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import me.tomasan7.jecnaapi.util.emptyMutableLinkedList
import java.util.*

/**
 * @property subjectPartsGrades Map of all [grades][Grade] for each subject part. (eg. "Teorie, Cvičení")
 * When there are no subject parts, the list of all [grades][Grade] is under `null` key.
 */
@Serializable
data class Grades private constructor(private val subjectPartsGrades: Map<String?, List<Grade>>)
{
    @Transient
    val subjectParts = subjectPartsGrades.keys

    @Transient
    val count = subjectParts.flatMap { subjectPartsGrades[it]!! }.size

    /**
     * @return True, when there are no [grades][Grade] in any subject part.
     */
    fun isEmpty(): Boolean
    {
        for (subjectPart in subjectParts)
        {
            if (get(subjectPart)!!.isNotEmpty())
                return false
        }

        return true
    }

    /**
     * @return False, when there are no [grades][Grade] in any subject part.
     */
    fun isNotEmpty() = !isEmpty()

    operator fun get(subjectPart: String?) = subjectPartsGrades[subjectPart]

    /**
     * @return This [Grades] average calculated by the [averageCalculator].
     */
    fun average(averageCalculator: GradeAverageCalculator = WeightedGradeAverageCalculator) = averageCalculator(this)

    companion object
    {
        fun builder() = Builder()
    }

    class Builder
    {
        private val subjectPartsGrades = mutableMapOf<String?, MutableList<Grade>>()

        fun addGrade(subjectPart: String?, grade: Grade) = subjectPartsGrades.computeIfAbsent(subjectPart) { emptyMutableLinkedList() }.add(grade)

        fun setGrades(subjectPart: String?, grades: List<Grade>)
        {
            subjectPartsGrades[subjectPart] = LinkedList(grades)
        }

        fun build() = Grades(subjectPartsGrades)
    }
}