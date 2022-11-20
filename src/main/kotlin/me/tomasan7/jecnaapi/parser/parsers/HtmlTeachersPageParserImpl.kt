package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.data.schoolStaff.TeacherReference
import me.tomasan7.jecnaapi.data.schoolStaff.TeachersPage
import me.tomasan7.jecnaapi.parser.ParseException
import org.jsoup.Jsoup

internal object HtmlTeachersPageParserImpl : HtmlTeachersPageParser
{
    override fun parse(html: String): TeachersPage
    {
        try
        {
            val document = Jsoup.parse(html)

            val teachersPageBuilder = TeachersPage.builder()

            val teacherEles = document.select(".contentLeftColumn > ul a, .contentRightColumn > ul a")

            for (teacherEle in teacherEles)
            {
                val fullName = teacherEle.text()
                val tag = teacherEle.attr("href").let { it.substring(URL_PATH_START_REMOVE_LENGTH until it.length) }

                teachersPageBuilder.addTeacherReference(TeacherReference(fullName, tag))
            }

            return teachersPageBuilder.build()
        }
        catch (e: Exception)
        {
            throw ParseException(e)
        }
    }

    private const val URL_PATH_START_REMOVE_LENGTH = "/ucitel/".length
}