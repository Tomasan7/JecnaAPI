package me.tomasan7.jecnaapi.java

import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.future.future
import me.tomasan7.jecnaapi.JecnaClient
import me.tomasan7.jecnaapi.data.schoolStaff.TeacherReference
import me.tomasan7.jecnaapi.data.timetable.TimetablePage
import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnaapi.util.SchoolYearHalf
import me.tomasan7.jecnaapi.web.Auth
import me.tomasan7.jecnaapi.web.AuthenticationException
import me.tomasan7.jecnaapi.web.jecna.Role
import java.time.Month

/**
 * Wraps the [JecnaClient] class to make it better usable from Java.
 */
@OptIn(DelicateCoroutinesApi::class)
class JecnaClientJavaWrapper(autoLogin: Boolean = false)
{
    val wrappedClient: JecnaClient = JecnaClient(autoLogin)

    fun login(username: String, password: String) = login(Auth(username, password))

    fun login(auth: Auth) = GlobalScope.future { wrappedClient.login(auth) }

    fun isLoggedIn() = GlobalScope.future { wrappedClient.isLoggedIn() }

    fun logout() = GlobalScope.future { wrappedClient.logout() }

    fun getCookieValue(name: String) = GlobalScope.future { wrappedClient.getCookieValue(name) }

    fun getCookie(name: String) = GlobalScope.future { wrappedClient.getCookie(name) }

    fun getSessionCookie() = GlobalScope.future { wrappedClient.getSessionCookie() }

    fun setCookie(name: String, valueString: String) = GlobalScope.future { wrappedClient.setCookie(name, valueString) }

    fun getRole() = wrappedClient.role

    fun setRole(role: Role) = GlobalScope.future { wrappedClient.setRole(role) }

    fun getNewsPage() = GlobalScope.future { wrappedClient.getNewsPage() }

    fun getGradesPage() = GlobalScope.future { wrappedClient.getGradesPage() }

    fun getGradesPage(schoolYear: SchoolYear, schoolYearHalf: SchoolYearHalf) =
        GlobalScope.future { wrappedClient.getGradesPage(schoolYear, schoolYearHalf) }

    fun getTimetablePage() = GlobalScope.future { wrappedClient.getTimetablePage() }

    fun getTimetablePage(schoolYear: SchoolYear, periodOption: TimetablePage.PeriodOption? = null) =
        GlobalScope.future { wrappedClient.getTimetablePage(schoolYear, periodOption) }

    fun getAttendancePage() = GlobalScope.future { wrappedClient.getAttendancesPage() }

    fun getAttendancePage(schoolYear: SchoolYear, month: Int) =
        GlobalScope.future { wrappedClient.getAttendancesPage(schoolYear, month) }

    fun getAttendancePage(schoolYear: SchoolYear, month: Month) =
        GlobalScope.future { wrappedClient.getAttendancesPage(schoolYear, month) }

    fun getTeachersPage() = GlobalScope.future { wrappedClient.getTeachersPage() }

    fun getTeacher(teacherTag: String) = GlobalScope.future { wrappedClient.getTeacher(teacherTag) }

    fun getTeacher(teacherReference: TeacherReference) =
        GlobalScope.future { wrappedClient.getTeacher(teacherReference) }

    /** A query without any authentication (autologin) handling. */
    fun plainQuery(path: String, parameters: Parameters? = null) =
        GlobalScope.future { wrappedClient.plainQuery(path, parameters) }

    /**
     * Makes a request to the provided path. Responses may vary depending on whether user is logged in or not.
     *
     * @param path Relative path from the domain. Must include first slash.
     * @param parameters HTTP parameters, which will be sent URL encoded.
     * @throws AuthenticationException When the query fails because user is not authenticated.
     * @return The [HttpResponse].
     */
    fun query(path: String, parameters: Parameters? = null) =
        GlobalScope.future { wrappedClient.query(path, parameters) }

    /**
     * Makes a request to the provided path. Responses may vary depending on whether user is logged in or not.
     *
     * @param path Relative path from the domain. Must include first slash.
     * @param parameters HTTP parameters, which will be sent URL encoded.
     * @throws AuthenticationException When the query fails because user is not authenticated.
     * @return The [HttpResponse].
     */
    fun queryStringBody(path: String, parameters: Parameters? = null) =
        GlobalScope.future { wrappedClient.queryStringBody(path, parameters) }

    /** Closes the HTTP client. */
    fun close() = wrappedClient.close()

    fun getAutoLogin() = wrappedClient.autoLogin

    /** The last [time][java.time.Instant] a call to [login] was successful (returned `true`). */
    fun getLastSuccessfulLoginTime() = wrappedClient.lastSuccessfulLoginTime

    /** The [Auth], that was last used in a call to [login], which was successful (returned `true`). */
    fun getLastSuccessfulLoginAuth() = wrappedClient.lastSuccessfulLoginAuth
}
