package me.tomasan7.jecnaapi.data.attendance

import java.time.LocalTime

/**
 * Represents single pass by the school entrance.
 * It can be either enter, or exit.
 *
 * @property type [AttendanceType] about whether the person exited or entered.
 * @property time The time, the person entered/exited.
 */
data class Attendance(val type: AttendanceType, val time: LocalTime)