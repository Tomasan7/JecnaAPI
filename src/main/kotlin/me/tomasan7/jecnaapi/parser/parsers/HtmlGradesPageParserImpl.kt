package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.data.Name
import me.tomasan7.jecnaapi.data.grade.*
import me.tomasan7.jecnaapi.parser.ParseException
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Parses correct HTML to [GradesPage] instance.
 * **Beware: The grade's subject is taken from the table's row name, not from the grade's page!**
 */
class HtmlGradesPageParserImpl : HtmlGradesPageParser
{
    override fun parse(html: String): GradesPage
    {
        try
        {
            val gradesPageBuilder = GradesPage.builder()

            val document = Jsoup.parse(html)

            /* All the rows (tr) in the grades table. */
            val rowEles = document.select(".score > tbody > tr")

            lateinit var behaviour: Behaviour

            for (rowEle in rowEles)
            {
                /* The first column (th; the header column) containing the subject name. */
                val subjectEle = rowEle.selectFirst("th")!!
                /* The second column (td; the first body column) with the main content. (subject parts, grades, commendations) */
                val mainColumnEle = rowEle.selectFirst("td")!!

                val subjectName = parseSubjectName(subjectEle.text())

                /* If this row's subject name is the behaviour one, parse this row as behaviour. */
                if (subjectName.full == Behaviour.SUBJECT_NAME)
                    behaviour = Behaviour(parseBehaviourNotifications(mainColumnEle),
                                          parseFinalGrade(rowEle.findFinalGradeEle()!!, subjectName))
                else
                    gradesPageBuilder.addSubject(Subject(subjectName,
                                                         parseSubjectGrades(mainColumnEle, subjectName),
                                                         parseFinalGrade(rowEle.findFinalGradeEle()!!, subjectName)))
            }

            gradesPageBuilder.setBehaviour(behaviour)

            return gradesPageBuilder.build()
        }
        catch (e: Exception)
        {
            throw ParseException(e)
        }
    }

    /**
     * Parses the [grades][Subject.Grades] from the main content column.
     *
     * @param gradesColumnEle The main content column.
     * @param subjectName The [name][Name] of the subject this grades are in.
     * @return The parsed [grades][Subject.Grades].
     */
    private fun parseSubjectGrades(gradesColumnEle: Element, subjectName: Name): Subject.Grades
    {
        val subjectGradesBuilder = Subject.Grades.builder()

        /* All the elements in the main content column. (either grade or subject part) */
        val columnContentEles = gradesColumnEle.selectFirst("td")!!.children()

        /* The last encountered subject part, so we know where the following grades belong. */
        var lastSubjectPart: String? = null

        for (contentEle in columnContentEles)
        {
            if (contentEle.classNames().contains("subjectPart"))
                /* The substring removes the colon (':') after the subject part. */
                lastSubjectPart = contentEle.text().let { it.substring(0, it.length - 1) }
            else if (contentEle.`is`("a"))
                subjectGradesBuilder.addGrade(lastSubjectPart, parseGrade(contentEle, subjectName))
        }

        return subjectGradesBuilder.build()
    }

    /**
     * Parses the [notifications][Behaviour.Notification] from the main content column.
     *
     * @param behaviourColumnEle The main content column.
     * @return The list of parsed [notifications][Behaviour.Notification].
     */
    private fun parseBehaviourNotifications(behaviourColumnEle: Element): List<Behaviour.Notification>
    {
        /* All the notification elements (a) in the main content column. */
        val notificationEles = behaviourColumnEle.select("span > a")

        val notifications = mutableListOf<Behaviour.Notification>()

        for (notificationEle in notificationEles)
        {
            /* The element of the icon (tick or cross) */
            val iconEle = notificationEle.selectFirst(".sprite-icon-16")!!

            /* Choosing type based on it's icon. (tick = good; cross = bad) */
            val type = if (iconEle.classNames().contains("sprite-icon-tick-16"))
                Behaviour.NotificationType.GOOD
            else
                Behaviour.NotificationType.BAD

            val message = notificationEle.selectFirst(".label")!!.text()

            notifications.add(Behaviour.Notification(type, message))
        }

        return notifications.toList()
    }

    /**
     * Parses [FinalGrade] from it's HTML element.
     *
     * @return The parsed [FinalGrade].
     */
    private fun parseFinalGrade(finalGradeEle: Element, subjectName: Name) = FinalGrade(finalGradeEle.text().toInt(), subjectName)

    /**
     * Finds the [FinalGrade]'s HTML element in the subject row element.
     *
     * @receiver The subject row element.
     * @return The [FinalGrade]'s HTML element.
     */
    private fun Element.findFinalGradeEle() = selectFirst(".scoreFinal")

    /**
     * Parses a [Grade] from it's HTML element.
     *
     * @return The parsed [Grade].
     */
    private fun parseGrade(gradeEle: Element, subjectName: Name): Grade
    {
        val valueChar = gradeEle.selectFirst(".value")!!.text()[0]
        val small = gradeEle.classNames().contains("scoreSmall")

        /* The title attribute of the grade element, which contains all the details. (description, date and teacher) */
        val titleAttr = gradeEle.attr("title")

        val teacher = TEACHER_REGEX.find(titleAttr)?.value
        val description = DESCRIPTION_REGEX.find(titleAttr)?.value
        val receiveDate = DATE_REGEX.find(titleAttr)?.value?.let { LocalDate.parse(it, DateTimeFormatter.ofPattern("dd.MM.yyyy")) }

        return Grade(valueChar, small, subjectName, teacher, description, receiveDate)
    }

    /**
     * Converts a [String] in "`name (shortname)`" format to a [Name] object.
     *
     * @return The [Name] instance.
     */
    private fun parseSubjectName(subjectNameStr: String): Name
    {
        val subjectSplit = subjectNameStr.split(SUBJECT_SHORT_SPLIT_REGEX, 2)
        val subjectFullName = subjectSplit[0]
        /* example value: "(IT)" */
        val subjectShortNameWithBrackets = subjectSplit.getOrNull(1)
        /* Removes the brackets from the string. (IT) -> IT */
        val subjectShortName = subjectShortNameWithBrackets?.substring(1, subjectShortNameWithBrackets.length - 1)

        return Name(subjectFullName, subjectShortName)
    }

    companion object
    {
        /**
         * Matches the description in a [Grade]'s HTML element title.
         *
         * Matches everything before last '(' preceded by a space.
         */
        private val DESCRIPTION_REGEX = Regex(""".*(?= \((?!.*\())""", RegexOption.DOT_MATCHES_ALL)

        /**
         * Matches the date in a [Grade]'s HTML element title.
         *
         * Matches everything between last '(' and first ',' after it.
         */
        private val DATE_REGEX = Regex("""(?<=\((?!.{0,100}\())[^,]*(?=,)""", RegexOption.DOT_MATCHES_ALL)

        /**
         * Matches the teacher in a [Grade]'s HTML element title.
         *
         * Matches everything between the first ',' followed by a space after last '(' and ending ')'
         */
        private val TEACHER_REGEX = Regex("""(?<=(?<=\((?!.{0,100}\()[^,]{0,100}), ).*(?=\)${'$'})""", RegexOption.DOT_MATCHES_ALL)

        /**
         *  Matches the space between subject name and it's short name in brackets.
         *  Used for splitting the name into full and short.
         */
        private val SUBJECT_SHORT_SPLIT_REGEX = Regex(""" (?=\(\w{1,4}\)${'$'})""")
    }
}