@file:UseSerializers(LocalDateSerializer::class)

package me.tomasan7.jecnaapi.data.attendance

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import me.tomasan7.jecnaapi.serialization.LocalDateSerializer
import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnaapi.util.emptyMutableLinkedList
import java.time.LocalDate
import java.time.Month

/**
 * Holds all [attendances][Attendance] for each day.
 */
@Serializable
data class AttendancesPage private constructor(
    private val attendances: Map<LocalDate, List<Attendance>>,
    val selectedSchoolYear: SchoolYear,
    val selectedMonth: Month
)
{
    /**
     * All days, this [AttendancesPage] has data for.
     */
    @Transient
    val days = attendances.keys

    /**
     * @return All [attendances][Attendance] for the provided day. Returns an empty list when no data for [day] is present.
     */
    operator fun get(day: LocalDate) = attendances.getOrDefault(day, emptyList())

    class Builder
    {
        private val attendances: MutableMap<LocalDate, MutableList<Attendance>> = HashMap()
        private lateinit var selectedSchoolYear: SchoolYear
        private lateinit var selectedMonth: Month

        /**
         * Adds [Attendance].
         *
         * @param day        The day to add this attendance to.
         * @param attendance The [Attendance] to add.
         */
        fun addAttendance(day: LocalDate, attendance: Attendance): Builder
        {
            /* Gets the list for the day, if none is present, creates a new list and puts it into the map. Then the attendance is added to that list. */
            attendances.computeIfAbsent(day) { emptyMutableLinkedList() }.add(attendance)
            return this
        }

        /**
         * Sets the [attendanceList] to the [day].
         */
        fun setAttendances(day: LocalDate, attendanceList: List<Attendance>): Builder
        {
            attendances[day] = attendanceList.toMutableList()
            return this
        }

        fun setSelectedSchoolYear(selectedSchoolYear: SchoolYear): Builder
        {
            this.selectedSchoolYear = selectedSchoolYear
            return this
        }

        fun setSelectedMonth(selectedMonth: Month): Builder
        {
            this.selectedMonth = selectedMonth
            return this
        }

        fun build(): AttendancesPage
        {
            check(::selectedSchoolYear.isInitialized) { "selectedSchoolYear has not been set." }
            check(::selectedMonth.isInitialized) { "selectedMonth  has not been set." }

            return AttendancesPage(attendances, selectedSchoolYear, selectedMonth)
        }
    }

    companion object
    {
        /**
         * Represents an [Attendance] [List] as a [String].
         * Does so by joining all [Attendance.toString]'s with a comma.
         *
         * @receiver The [Attendance] [List] to represent.
         * @return The [String] representation.
         */
        fun List<Attendance>.toAttendanceString() = joinToString()

        fun builder() = Builder()
    }
}
