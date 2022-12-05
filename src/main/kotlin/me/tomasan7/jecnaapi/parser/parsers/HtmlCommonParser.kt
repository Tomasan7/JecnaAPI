package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnaapi.util.toSchoolYear
import org.jsoup.nodes.Document
import java.text.SimpleDateFormat
import java.time.Month
import java.util.*

/**
 * Functions used by multiple parsers.
 */
object HtmlCommonParser
{
    fun parseSelectedSchoolYear(document: Document): SchoolYear
    {
        val selectedSchoolYearEle = document.selectFirst("#schoolYearId > option[selected]")!!
        return selectedSchoolYearEle.text().toSchoolYear()
    }

    fun parseSelectedMonth(document: Document): Month
    {
        val selectedMonthEle = document.selectFirst("#schoolYearPartMonthId > option[selected]")!!
        return parseMonthByName(selectedMonthEle.text())
    }

    fun getSelectSelectedValue(document: Document, selectId: String) = document.selectFirst("#$selectId > option[selected]")

    private fun parseMonthByName(monthName: String) = when(monthName)
    {
        "leden" -> Month.JANUARY
        "únor" -> Month.FEBRUARY
        "březen" -> Month.MARCH
        "duben" -> Month.APRIL
        "květen" -> Month.MAY
        "červen" -> Month.JUNE
        "červenec" -> Month.JULY
        "srpen" -> Month.AUGUST
        "září" -> Month.SEPTEMBER
        "říjen" -> Month.OCTOBER
        "listopad" -> Month.NOVEMBER
        "prosinec" -> Month.DECEMBER
        else -> throw IllegalArgumentException("Unknown month name: $monthName")
    }

    private val MONTH_DATE_FORMAT = SimpleDateFormat("MMMM", Locale.forLanguageTag("cs-CZ"))
}