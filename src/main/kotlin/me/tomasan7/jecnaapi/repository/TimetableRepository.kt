package me.tomasan7.jecnaapi.repository

import me.tomasan7.jecnaapi.data.SchoolYear
import me.tomasan7.jecnaapi.data.TimetablePage

/**
 * Retrieves [TimetablePage] from any kind of data source.
 */
interface TimetableRepository
{
    suspend fun queryTimetable(): TimetablePage

    /**
     * @param schoolYear The [SchoolYear] to get the [TimetablePage] for.
     */
    suspend fun queryTimetable(schoolYear: SchoolYear): TimetablePage
}