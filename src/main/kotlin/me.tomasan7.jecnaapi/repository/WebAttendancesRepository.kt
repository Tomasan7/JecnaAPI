package me.tomasan7.jecnaapi.repository

import io.ktor.http.*
import me.tomasan7.jecnaapi.data.Attendances
import me.tomasan7.jecnaapi.data.SchoolYear
import me.tomasan7.jecnaapi.parser.parsers.HtmlAttendancesParser
import me.tomasan7.jecnaapi.util.JecnaPeriodEncoder
import me.tomasan7.jecnaapi.util.JecnaPeriodEncoder.jecnaEncode
import me.tomasan7.jecnaapi.web.JecnaWebClient
import me.tomasan7.jecnaapi.web.append

/**
 * Retrieves [Attendances] from the Ječná web.
 */
class WebAttendancesRepository(private val webClient: JecnaWebClient) : AttendancesRepository
{
    private val attendancesParser = HtmlAttendancesParser()

    override suspend fun queryAttendances() = attendancesParser.parse(webClient.queryStringBody(WEB_PATH))

    override suspend fun queryAttendances(schoolYear: SchoolYear, month: Int) =
        attendancesParser.parse(webClient.queryStringBody(WEB_PATH, Parameters.build {
            append(schoolYear.jecnaEncode())
            append(JecnaPeriodEncoder.encodeMonth(month))
        }))

    companion object
    {
        private const val WEB_PATH = "/absence/passing-student"
    }
}