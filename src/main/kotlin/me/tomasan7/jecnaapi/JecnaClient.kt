package me.tomasan7.jecnaapi

import io.ktor.client.statement.*
import io.ktor.http.*
import me.tomasan7.jecnaapi.data.timetable.TimetablePage
import me.tomasan7.jecnaapi.parser.parsers.*
import me.tomasan7.jecnaapi.util.JecnaPeriodEncoder
import me.tomasan7.jecnaapi.util.JecnaPeriodEncoder.jecnaEncode
import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnaapi.util.SchoolYearHalf
import me.tomasan7.jecnaapi.web.Auth
import me.tomasan7.jecnaapi.web.JecnaWebClient
import me.tomasan7.jecnaapi.web.JecnaWebClient.AuthenticationException
import me.tomasan7.jecnaapi.web.append
import java.time.Month

/**
 * A client to access Jecna Web data.
 *
 * @param autoLogin Saves provided [Auth] on each [login] call.
 * Then when calling [query] and it fails because of [AuthenticationException], [login] is called with the saved [Auth] and the request retried.
 */
class JecnaClient(autoLogin: Boolean = false)
{
    private val webClient = JecnaWebClient(autoLogin)

    var autoLogin: Boolean by webClient::autoLogin

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

    suspend fun getNewsPage() = newsPageParser.parse(queryStringBody(PageWebPath.news))

    suspend fun getGradesPage(schoolYear: SchoolYear, schoolYearHalf: SchoolYearHalf) =
        gradesPageParser.parse(queryStringBody(PageWebPath.grades, Parameters.build {
            append(schoolYear.jecnaEncode())
            append(schoolYearHalf.jecnaEncode())
        }))

    suspend fun getGradesPage() = gradesPageParser.parse(queryStringBody(PageWebPath.grades))

    suspend fun getTimetablePage(schoolYear: SchoolYear, periodOption: TimetablePage.PeriodOption? = null) =
        timetablePageParser.parse(queryStringBody(PageWebPath.timetable, Parameters.build {
            append(schoolYear.jecnaEncode())
            periodOption?.let { append(it.jecnaEncode()) }
        }))

    suspend fun getTimetablePage() = timetablePageParser.parse(queryStringBody(PageWebPath.timetable))

    suspend fun getAttendancesPage(schoolYear: SchoolYear, month: Month) = getAttendancesPage(schoolYear, month.value)

    suspend fun getAttendancesPage(schoolYear: SchoolYear, month: Int) =
        attendancesPageParser.parse(queryStringBody(PageWebPath.attendances, Parameters.build {
            append(schoolYear.jecnaEncode())
            append(JecnaPeriodEncoder.encodeMonth(month))
        }))

    suspend fun getAttendancesPage() = attendancesPageParser.parse(queryStringBody(PageWebPath.attendances))

    /**
     * Makes a request to the provided path. Response may vary depending on whether user is logged in or not.
     *
     * @param path Relative path from the domain. Must include first slash.
     * @param parameters HTTP parameters, which will be sent URL encoded.
     * @return The [HttpResponse].
     */
    suspend fun query(path: String, parameters: Parameters? = null) = webClient.query(path, parameters)

    /**
     * Makes a request to the provided path. Response may vary depending on whether user is logged in or not.
     *
     * @param path Relative path from the domain. Must include first slash.
     * @param parameters HTTP parameters, which will be sent URL encoded.
     * @return The [HttpResponse].
     */
    suspend fun queryStringBody(path: String, parameters: Parameters? = null) = webClient.queryStringBody(path, parameters)

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