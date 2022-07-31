package me.tomasan7.jecnaapi.util

import java.time.LocalDate
import java.time.Month

enum class SchoolYearHalf
{
    FIRST,
    SECOND;

    companion object
    {
        /**
         * @return Current [SchoolYearHalf].
         */
        fun current() = if (LocalDate.now().month !in Month.FEBRUARY..Month.AUGUST)
            SchoolYearHalf.FIRST
        else
            SchoolYearHalf.SECOND
    }
}