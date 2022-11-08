package me.tomasan7.jecnaapi

import me.tomasan7.jecnaapi.data.timetable.TimetablePage
import me.tomasan7.jecnaapi.repository.WebAttendancesRepository
import me.tomasan7.jecnaapi.repository.WebGradesRepository
import me.tomasan7.jecnaapi.repository.WebNewsRepository
import me.tomasan7.jecnaapi.repository.WebTimetableRepository
import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnaapi.util.SchoolYearHalf
import me.tomasan7.jecnaapi.web.Auth
import me.tomasan7.jecnaapi.web.JecnaWebClient
import java.time.Month

class JecnaClient
{
    private val jecnaWebClient = JecnaWebClient()

    val lastLoginAuth: Auth?
        get() = jecnaWebClient.lastLoginAuth

    private val gradesRepository = WebGradesRepository(jecnaWebClient)
    private val newsRepository = WebNewsRepository(jecnaWebClient)
    private val timetableRepository = WebTimetableRepository(jecnaWebClient)
    private val attendancesRepository = WebAttendancesRepository(jecnaWebClient)

    suspend fun login(username: String, password: String) = login(Auth(username, password))

    suspend fun login(auth: Auth) = jecnaWebClient.login(auth)

    suspend fun logout() = jecnaWebClient.logout()

    suspend fun isLoggedIn() = jecnaWebClient.isLoggedIn()

    suspend fun getGradesPage(schoolYear: SchoolYear, schoolYearHalf: SchoolYearHalf) = gradesRepository.queryGradesPage(schoolYear, schoolYearHalf)

    suspend fun getGradesPage() = gradesRepository.queryGradesPage()

    suspend fun getNewsPage() = newsRepository.queryNewsPage()

    suspend fun getTimetablePage(schoolYear: SchoolYear, periodOption: TimetablePage.PeriodOption? = null) = timetableRepository.queryTimetablePage(schoolYear, periodOption)

    suspend fun getTimetablePage() = timetableRepository.queryTimetablePage()

    suspend fun getAttendancesPage(schoolYear: SchoolYear, month: Month) = attendancesRepository.queryAttendancesPage(schoolYear, month)

    suspend fun getAttendancesPage(schoolYear: SchoolYear, monthId: Int) = attendancesRepository.queryAttendancesPage(schoolYear, monthId)

    suspend fun getAttendancesPage() = attendancesRepository.queryAttendancesPage()
}