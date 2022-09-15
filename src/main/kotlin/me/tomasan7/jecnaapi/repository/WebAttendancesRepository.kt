package me.tomasan7.jecnaapi.repository

import io.ktor.http.*
import me.tomasan7.jecnaapi.data.attendance.AttendancesPage
import me.tomasan7.jecnaapi.parser.parsers.HtmlAttendancesPageParser
import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnaapi.util.JecnaPeriodEncoder
import me.tomasan7.jecnaapi.util.JecnaPeriodEncoder.jecnaEncode
import me.tomasan7.jecnaapi.web.JecnaWebClient
import me.tomasan7.jecnaapi.web.append

/**
 * Retrieves [AttendancesPage] from the Ječná web.
 */
class WebAttendancesRepository(
    private val webClient: JecnaWebClient,
    private val attendancesParser: HtmlAttendancesPageParser
) : AttendancesRepository
{
    override suspend fun queryAttendancesPage() = attendancesParser.parse(webClient.queryStringBody(WEB_PATH))

    override suspend fun queryAttendancesPage(schoolYear: SchoolYear, month: Int) =
        attendancesParser.parse(webClient.queryStringBody(WEB_PATH, Parameters.build {
            append(schoolYear.jecnaEncode())
            append(JecnaPeriodEncoder.encodeMonth(month))
        }))

    companion object
    {
        private const val WEB_PATH = "/absence/passing-student"
    }
}