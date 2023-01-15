package me.tomasan7.jecnaapi.data.grade

import kotlinx.serialization.Serializable

/**
 * Represents the behaviour row in the grades table.
 */
@Serializable
data class Behaviour(
    val notifications: List<Notification>,
    val finalGrade: FinalGrade
)
{
    companion object
    {
        const val SUBJECT_NAME = "Chování"
    }

    /**
     * Represents single notification in the row about the student's behaviour.
     */
    @Serializable
    data class Notification(val type: NotificationType, val message: String)

    /**
     * Type of the [Notification], either [GOOD] or [BAD].
     */
    enum class NotificationType
    {
        GOOD,
        BAD
    }
}