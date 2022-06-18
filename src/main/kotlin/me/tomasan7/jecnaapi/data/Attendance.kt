package me.tomasan7.jecnaapi.data

import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Represents single pass by the school entrance.
 * It can be either enter, or exit.
 *
 * @property exit Whether the person exited or entered. `false` for enter, `true` for exit.
 * @property time The time, the person entered/exited.
 */
class Attendance(val exit: Boolean, val time: LocalTime)
{
    override fun toString() = (if (exit) "Odchod" else "Příchod") + " " + time.format(DateTimeFormatter.ofPattern("HH:mm"))
}