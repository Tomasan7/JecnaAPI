package me.tomasan7.jecnaapi

import io.ktor.http.*
import me.tomasan7.jecnaapi.data.timetable.TimetablePage
import me.tomasan7.jecnaapi.parser.parsers.*
import me.tomasan7.jecnaapi.util.JecnaPeriodEncoder
import me.tomasan7.jecnaapi.util.JecnaPeriodEncoder.jecnaEncode
import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnaapi.util.SchoolYearHalf
import me.tomasan7.jecnaapi.web.Auth
import me.tomasan7.jecnaapi.web.JecnaWebClient
import me.tomasan7.jecnaapi.web.append
import java.time.Month

class JecnaClient
{
    private val webClient = JecnaWebClient()

    /**
     * The [Auth], that was last used in a call to [login]. (also the one with two parameters)
     */
    val lastLoginAuth: Auth?
        get() = webClient.lastLoginAuth

    private val newsPageParser: HtmlNewsPageParser = HtmlNewsPageParserImpl
    private val gradesPageParser: HtmlGradesPageParser = HtmlGradesPageParserImpl
    private val timetablePageParser: HtmlTimetablePageParser = HtmlTimetableParserImpl
    private val attendancesPageParser: HtmlAttendancesPageParser = HtmlAttendancesPageParserImpl

    suspend fun login(username: String, password: String) = login(Auth(username, password))

    suspend fun login(auth: Auth) = webClient.login(auth)

    suspend fun logout() = webClient.logout()

    suspend fun isLoggedIn() = webClient.isLoggedIn()

    suspend fun getNewsPage() = newsPageParser.parse(webClient.queryStringBody(PageWebPath.news))

    suspend fun getGradesPage(schoolYear: SchoolYear, schoolYearHalf: SchoolYearHalf) =
        gradesPageParser.parse(webClient.queryStringBody(PageWebPath.grades, Parameters.build {
            append(schoolYear.jecnaEncode())
            append(schoolYearHalf.jecnaEncode())
        }))

    suspend fun getGradesPage() = gradesPageParser.parse(webClient.queryStringBody(PageWebPath.grades))

    suspend fun getTimetablePage(schoolYear: SchoolYear, periodOption: TimetablePage.PeriodOption? = null) =
        timetablePageParser.parse(webClient.queryStringBody(PageWebPath.timetable, Parameters.build {
            append(schoolYear.jecnaEncode())
            periodOption?.let { append(it.jecnaEncode()) }
        }))

    suspend fun getTimetablePage() = timetablePageParser.parse(webClient.queryStringBody(PageWebPath.timetable))

    suspend fun getAttendancesPage(schoolYear: SchoolYear, month: Month) = getAttendancesPage(schoolYear, month.value)

    suspend fun getAttendancesPage(schoolYear: SchoolYear, month: Int) =
        attendancesPageParser.parse(webClient.queryStringBody(PageWebPath.attendances, Parameters.build {
            append(schoolYear.jecnaEncode())
            append(JecnaPeriodEncoder.encodeMonth(month))
        }))

    suspend fun getAttendancesPage() = attendancesPageParser.parse(webClient.queryStringBody(PageWebPath.attendances))

    companion object
    {
        private object PageWebPath
        {
            const val news = "/"
            const val grades = "/score/student"
            const val timetable = "/timetable/class"
            const val attendances = "/absence/passing-student"
        }
    }
}