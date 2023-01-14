package me.tomasan7.jecnaapi.data.schoolStaff

import me.tomasan7.jecnaapi.util.setAll

data class TeachersPage(val teachersReferences: Set<TeacherReference>)
{
    companion object
    {
        fun builder() = Builder()
    }

    class Builder
    {
        private val teacherReferences = mutableSetOf<TeacherReference>()

        fun addTeacherReference(teacherReference: TeacherReference): Builder
        {
            teacherReferences.add(teacherReference)
            return this
        }

        fun setArticles(teachersReferences: Set<TeacherReference>): Builder
        {
            this.teacherReferences.setAll(teachersReferences)
            return this
        }

        fun build() = TeachersPage(teacherReferences)
    }
}