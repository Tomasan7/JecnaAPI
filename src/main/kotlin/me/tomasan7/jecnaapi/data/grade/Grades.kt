package me.tomasan7.jecnaapi.data.grade

import me.tomasan7.jecnaapi.util.emptyMutableLinkedList

/**
 * @property subjectPartsGrades Map of all [grades][Grade] for each subject part. (eg. "Teorie, Cvičení")
 * When there are no subject parts, the list of all [grades][Grade] is under `null` key.
 */
class Grades private constructor(private val subjectPartsGrades: Map<String?, List<Grade>>)
{
    val subjectParts = subjectPartsGrades.keys

    operator fun get (subjectPart: String?) = subjectPartsGrades[subjectPart]

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
            subjectPartsGrades[subjectPart] = emptyMutableLinkedList()
        }

        fun build() = Grades(subjectPartsGrades)
    }
}