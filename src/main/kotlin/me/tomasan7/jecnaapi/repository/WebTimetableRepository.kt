package me.tomasan7.jecnaapi.repository

import io.ktor.http.*
import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnaapi.data.timetable.TimetablePage
import me.tomasan7.jecnaapi.parser.parsers.HtmlTimetableParserImpl
import me.tomasan7.jecnaapi.util.JecnaPeriodEncoder.jecnaEncode
import me.tomasan7.jecnaapi.web.JecnaWebClient
import me.tomasan7.jecnaapi.web.append

/**
 * Retrieves [TimetablePage] from the Ječná web.
 */
class WebTimetableRepository(private val webClient: JecnaWebClient) : TimetableRepository
{
    private val timetableParser = HtmlTimetableParserImpl()

    override suspend fun queryTimetablePage() = timetableParser.parse(webClient.queryStringBody(WEB_PATH))

    override suspend fun queryTimetablePage(schoolYear: SchoolYear, periodOption: TimetablePage.PeriodOption?) =
        timetableParser.parse(webClient.queryStringBody(WEB_PATH, Parameters.build {
            append(schoolYear.jecnaEncode())
            periodOption?.let { append(it.jecnaEncode()) }
        }))

    companion object
    {
        private const val WEB_PATH = "/timetable/class"
    }
}