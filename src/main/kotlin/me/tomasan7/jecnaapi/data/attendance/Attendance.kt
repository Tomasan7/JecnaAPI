package me.tomasan7.jecnaapi.data.attendance

import kotlinx.serialization.Serializable
import me.tomasan7.jecnaapi.serialization.LocalTimeSerializer
import java.time.LocalTime

/**
 * Represents single pass by the school entrance.
 * It can be either enter, or exit.
 *
 * @property type [AttendanceType] about whether the person exited or entered.
 * @property time The time, the person entered/exited.
 */
@Serializable
data class Attendance(
    val type: AttendanceType,
    @Serializable(with = LocalTimeSerializer::class)
    val time: LocalTime
)