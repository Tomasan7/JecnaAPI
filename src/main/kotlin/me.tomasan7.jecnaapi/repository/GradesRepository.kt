package me.tomasan7.jecnaapi.repository

import me.tomasan7.jecnaapi.data.GradesPage
import me.tomasan7.jecnaapi.data.SchoolYear

/**
 * Retrieves [GradesPage] from any kind of data source.
 */
interface GradesRepository
{
    suspend fun queryGrades(): GradesPage

    /**
     * @param schoolYear The [SchoolYear] to get the [GradesPage] for.
     * @param firstHalf The school year half to get the [GradesPage] for. `true` for first half and `false` for second half.
     */
    suspend fun queryGrades(schoolYear: SchoolYear, firstHalf: Boolean): GradesPage
}