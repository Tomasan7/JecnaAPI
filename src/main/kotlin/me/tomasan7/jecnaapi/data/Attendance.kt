package me.tomasan7.jecnaapi.data

import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Represents single pass by the school entrance.
 * It can be either enter, or exit.
 *
 * @property type [AttendanceType] about whether the person exited or entered.
 * @property time The time, the person entered/exited.
 */
class Attendance(val type: AttendanceType, val time: LocalTime)
{
    override fun toString() = (if (type == AttendanceType.ENTER) "Příchod" else "Odchod") + " " + time.format(DateTimeFormatter.ofPattern("HH:mm"))
}