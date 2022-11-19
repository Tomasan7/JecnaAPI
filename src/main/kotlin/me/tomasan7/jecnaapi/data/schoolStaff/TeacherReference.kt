package me.tomasan7.jecnaapi.data.schoolStaff

class TeacherReference(
    val fullName: String,
    tag: String
)
{
    val tag = tag.trim().lowercase().replaceFirstChar { it.uppercaseChar() }

    override fun equals(other: Any?): Boolean
    {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TeacherReference

        if (fullName != other.fullName) return false
        if (tag != other.tag) return false

        return true
    }

    override fun hashCode(): Int
    {
        var result = fullName.hashCode()
        result = 31 * result + tag.hashCode()
        return result
    }
}