package me.tomasan7.jecnaapi.repository

import me.tomasan7.jecnaapi.data.timetable.TimetablePage
import me.tomasan7.jecnaapi.util.SchoolYear

/**
 * Retrieves [TimetablePage] from any kind of data source.
 */
interface TimetableRepository
{
    suspend fun queryTimetablePage(): TimetablePage

    /**
     * @param schoolYear The [SchoolYear] to get the [TimetablePage] for.
     * @param periodOption The [TimetablePage.PeriodOption] in the school year to get [TimetablePage] for.
     */
    suspend fun queryTimetablePage(schoolYear: SchoolYear, periodOption: TimetablePage.PeriodOption? = null): TimetablePage
}