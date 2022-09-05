package me.tomasan7.jecnaapi.repository

import me.tomasan7.jecnaapi.data.attendance.AttendancesPage
import me.tomasan7.jecnaapi.util.SchoolYear

/**
 * Retrieves [AttendancesPage] from any kind of data source.
 */
interface AttendancesRepository
{
    suspend fun queryAttendancesPage(): AttendancesPage

    /**
     * @param schoolYear The [SchoolYear] to get the [AttendancesPage] for.
     * @param month The month to get the [AttendancesPage] for. (`1` = January)
     */
    suspend fun queryAttendancesPage(schoolYear: SchoolYear, month: Int): AttendancesPage
}