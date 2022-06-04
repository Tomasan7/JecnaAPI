package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.data.Grade
import me.tomasan7.jecnaapi.data.Grades
import me.tomasan7.jecnaapi.parser.ParseException
import org.jsoup.Jsoup
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.system.measureTimeMillis
import kotlin.time.measureTime

/**
 * Parses correct HTML to [Grades] instance.
 * **Beware: The grade's subject is taken from the table's row name, not from the grade's page!**
 */
class HtmlGradesParser : GradesParser
{
    override fun parse(source: String): Grades
    {
        try
        {
            val gradesBuilder = Grades.builder()

            val document = Jsoup.parse(source)

            /* All the rows (tr) in the grades table. */
            val rowEles = document.select(".score > tbody > tr")

            for (rowEle in rowEles)
            {
                /* The first column in the row, which contains the subject name. */
                val subjectEle = rowEle.selectFirst("th")!!
                /* All the grade elements in the second column of the row. (not finalGrades) */
                val gradeEles = rowEle.select("td > a.score:not(.scoreFinal)")

                for (gradeEle in gradeEles)
                {
                    val valueString = gradeEle.selectFirst(".value")!!.text()
                    val value = valueString[0]
                    val small = gradeEle.classNames().contains("scoreSmall")

                    /* The title attribute of the grade element, which contains all the details. (description, date and teacher) */
                    val titleAttr = gradeEle.attr("title")

                    val subject = subjectEle.text()
                    val teacher = TEACHER_REGEX.find(titleAttr)?.value
                    val description = DESCRIPTION_REGEX.find(titleAttr)?.value
                    val receiveDate = DATE_REGEX.find(titleAttr)?.value?.let { LocalDate.parse(it, DateTimeFormatter.ofPattern("dd.MM.yyyy")) }

                    gradesBuilder.addGrade(subjectEle.text(), Grade(value, small, subject, teacher, description, receiveDate))
                }
            }
            return gradesBuilder.build()
        }
        catch (e: Exception)
        {
            throw ParseException(e)
        }
    }

    companion object
    {
        /* Matches everything before last '(' preceded by a space. */
        private val DESCRIPTION_REGEX = Regex(".*(?= \\((?!.*\\())", RegexOption.DOT_MATCHES_ALL)

        /* Matches everything between last '(' and first ',' after it. */
        private val DATE_REGEX = Regex("(?<=\\((?!.{0,100}\\())[^,]*(?=,)", RegexOption.DOT_MATCHES_ALL)

        /* Matches everything between the first ',' followed by a space after last '(' and ending ')' */
        private val TEACHER_REGEX = Regex("(?<=(?<=\\((?!.{0,100}\\()[^,]{0,100}), ).*(?=\\)$)", RegexOption.DOT_MATCHES_ALL)
    }
}