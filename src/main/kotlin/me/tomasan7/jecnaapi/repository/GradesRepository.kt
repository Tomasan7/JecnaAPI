package me.tomasan7.jecnaapi.repository

import me.tomasan7.jecnaapi.data.grade.GradesPage
import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnaapi.util.SchoolYearHalf

/**
 * Retrieves [GradesPage] from any kind of data source.
 */
interface GradesRepository
{
    suspend fun queryGradesPage(): GradesPage

    /**
     * @param schoolYear The [SchoolYear] to get the [GradesPage] for.
     * @param firstHalf The school year half to get the [GradesPage] for. `true` for first half and `false` for second half.
     */
    suspend fun queryGradesPage(schoolYear: SchoolYear, schoolYearHalf: SchoolYearHalf): GradesPage
}