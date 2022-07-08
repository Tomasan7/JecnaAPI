package me.tomasan7.jecnaapi.repository

import io.ktor.http.*
import me.tomasan7.jecnaapi.data.grade.GradesPage
import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnaapi.parser.parsers.HtmlGradesPageParserImpl
import me.tomasan7.jecnaapi.util.JecnaPeriodEncoder.jecnaEncode
import me.tomasan7.jecnaapi.util.SchoolYearHalf
import me.tomasan7.jecnaapi.web.JecnaWebClient
import me.tomasan7.jecnaapi.web.append

/**
 * Retrieves [GradesPage] from the Ječná web.
 */
class WebGradesRepository(private val webClient: JecnaWebClient) : GradesRepository
{
    private val gradesParser = HtmlGradesPageParserImpl()

    override suspend fun queryGradesPage() = gradesParser.parse(webClient.queryStringBody(WEB_PATH))

    override suspend fun queryGradesPage(schoolYear: SchoolYear, schoolYearHalf: SchoolYearHalf) =
        gradesParser.parse(webClient.queryStringBody(WEB_PATH, Parameters.build {
            append(schoolYear.jecnaEncode())
            append(schoolYearHalf.jecnaEncode())
        }))

    companion object
    {
        private const val WEB_PATH = "/score/student"
    }
}