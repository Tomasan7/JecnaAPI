package me.tomasan7.jecnaapi.data

abstract class SchoolAttendee(
    val fullName: String,
    val username: String,
    val schoolMail: String,
    val privateMail: String? = null,
    val phoneNumbers: List<String> = emptyList(),
    val profilePicturePath: String? = null
)
