package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.data.Name
import me.tomasan7.jecnaapi.data.grade.FinalGrade
import me.tomasan7.jecnaapi.data.grade.Grade
import me.tomasan7.jecnaapi.data.grade.GradesPage
import me.tomasan7.jecnaapi.data.grade.Subject
import me.tomasan7.jecnaapi.parser.ParseException
import org.jsoup.Jsoup
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Parses correct HTML to [GradesPage] instance.
 * **Beware: The grade's subject is taken from the table's row name, not from the grade's page!**
 */
class HtmlGradesParser : GradesParser
{
    override fun parse(source: String): GradesPage
    {
        try
        {
            val gradesPageBuilder = GradesPage.builder()

            val document = Jsoup.parse(source)

            /* All the rows (tr) in the grades table. */
            val rowEles = document.select(".score > tbody > tr")

            for (rowEle in rowEles)
            {
                /* The first column in the row, which contains the subject name. */
                val subjectEle = rowEle.selectFirst("th")!!
                /* All the grade elements in the second column of the row. (not finalGrades) */
                val gradeEles = rowEle.select("td > a.score:not(.scoreFinal)")

                val subjectSplit = subjectEle.text().split(SUBJECT_SHORT_SPLIT_REGEX, 2)
                val subjectFullName = subjectSplit[0]
                val subjectShortNameWithBrackets = subjectSplit.getOrNull(1)
                /* Removes the brackets from the string. (IT) -> IT */
                val subjectShortName = subjectShortNameWithBrackets?.substring(1, subjectShortNameWithBrackets.length - 1)
                val subjectName = Name(subjectFullName, subjectShortName)

                val grades = mutableListOf<Grade>()

                for (gradeEle in gradeEles)
                {
                    val valueString = gradeEle.selectFirst(".value")!!.text()
                    val value = valueString[0]
                    val small = gradeEle.classNames().contains("scoreSmall")

                    /* The title attribute of the grade element, which contains all the details. (description, date and teacher) */
                    val titleAttr = gradeEle.attr("title")

                    val teacher = TEACHER_REGEX.find(titleAttr)?.value
                    val description = DESCRIPTION_REGEX.find(titleAttr)?.value
                    val receiveDate = DATE_REGEX.find(titleAttr)?.value?.let { LocalDate.parse(it, DateTimeFormatter.ofPattern("dd.MM.yyyy")) }

                    grades.add(Grade(value, small, subjectName, teacher, description, receiveDate))
                }

                val finalGradeEle = rowEle.selectFirst(".scoreFinal")
                val finalGrade = if (finalGradeEle != null) FinalGrade(finalGradeEle.text().toInt(), subjectName) else null

                gradesPageBuilder.addSubject(Subject(subjectName, grades, finalGrade))
            }
            return gradesPageBuilder.build()
        }
        catch (e: Exception)
        {
            throw ParseException(e)
        }
    }

    companion object
    {
        /* Matches everything before last '(' preceded by a space. */
        private val DESCRIPTION_REGEX = Regex(""".*(?= \((?!.*\())""", RegexOption.DOT_MATCHES_ALL)

        /* Matches everything between last '(' and first ',' after it. */
        private val DATE_REGEX = Regex("""(?<=\((?!.{0,100}\())[^,]*(?=,)""", RegexOption.DOT_MATCHES_ALL)

        /* Matches everything between the first ',' followed by a space after last '(' and ending ')' */
        private val TEACHER_REGEX = Regex("""(?<=(?<=\((?!.{0,100}\()[^,]{0,100}), ).*(?=\)${'$'})""", RegexOption.DOT_MATCHES_ALL)

        /* Matches the space between subject name and it's short name in brackets. */
        private val SUBJECT_SHORT_SPLIT_REGEX = Regex(""" (?=\(\w{1,4}\)${'$'})""")
    }
}