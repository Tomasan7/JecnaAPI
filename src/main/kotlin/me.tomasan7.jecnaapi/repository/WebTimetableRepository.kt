package me.tomasan7.jecnaapi.repository

import io.ktor.http.*
import me.tomasan7.jecnaapi.data.SchoolYear
import me.tomasan7.jecnaapi.data.Timetable
import me.tomasan7.jecnaapi.parser.parsers.HtmlTimetableParser
import me.tomasan7.jecnaapi.util.JecnaPeriodEncoder.jecnaEncode
import me.tomasan7.jecnaapi.web.JecnaWebClient
import me.tomasan7.jecnaapi.web.append

/**
 * Retrieves [Timetable] from the Ječná web.
 */
class WebTimetableRepository(private val webClient: JecnaWebClient) : TimetableRepository
{
    private val timetableParser = HtmlTimetableParser()

    override suspend fun queryTimetable() = timetableParser.parse(webClient.queryStringBody(WEB_PATH))

    override suspend fun queryTimetable(schoolYear: SchoolYear) =
        timetableParser.parse(webClient.queryStringBody(WEB_PATH, Parameters.build {
            append(schoolYear.jecnaEncode())
        }))

    companion object
    {
        private const val WEB_PATH = "/timetable/class"
    }
}