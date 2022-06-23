package me.tomasan7.jecnaapi.data.grade

data class Behaviour(val notifications: List<Notification>, val finalGrade: FinalGrade)
{
    companion object
    {
        const val SUBJECT_NAME = "Chování"
    }

    data class Notification(val type: NotificationType, val message: String)

    enum class NotificationType
    {
        GOOD,
        BAD
    }
}