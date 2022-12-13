package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.data.grade.*
import me.tomasan7.jecnaapi.parser.HtmlElementNotFoundException
import me.tomasan7.jecnaapi.parser.ParseException
import me.tomasan7.jecnaapi.util.Name
import me.tomasan7.jecnaapi.util.SchoolYearHalf
import me.tomasan7.jecnaapi.util.emptyMutableLinkedList
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Parses correct HTML to [GradesPage] instance.
 * **Beware: The grade's subject is taken from the table's row name, not from the grade's page!**
 */
internal object HtmlGradesPageParserImpl : HtmlGradesPageParser
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
                val subjectEle = rowEle.selectFirstOrThrow("th")
                /* The second column (td; the first body column) with the main content. (subject parts, grades, commendations) */
                val mainColumnEle = rowEle.selectFirstOrThrow("td")

                val subjectName = parseSubjectName(subjectEle.text())

                /* If this row's subject name is the behaviour one, parse this row as behaviour. */
                if (subjectName.full == Behaviour.SUBJECT_NAME)
                    behaviour = Behaviour(parseBehaviourNotifications(mainColumnEle),
                                          parseFinalGrade(rowEle.findFinalGradeEle().expectElement("behaviour final grade"), subjectName))
                else
                    gradesPageBuilder.addSubject(Subject(subjectName,
                                                         parseSubjectGrades(mainColumnEle, subjectName),
                                                         rowEle.findFinalGradeEle()?.let { parseFinalGrade(it, subjectName) }))
            }

            gradesPageBuilder.setBehaviour(behaviour)

            gradesPageBuilder.setSelectedSchoolYear(HtmlCommonParser.parseSelectedSchoolYear(document))
            gradesPageBuilder.setSelectedSchoolYearHalf(parseSelectedSchoolYearHalf(document))

            return gradesPageBuilder.build()
        }
        catch (e: ParseException)
        {
            throw e
        }
        catch (e: Exception)
        {
            throw ParseException("Failed to parse grades page.", e)
        }
    }

    /**
     * Parses the [grades][Subject.Grades] from the main content column.
     *
     * @param gradesColumnEle The main content column.
     * @param subjectName The [name][Name] of the subject this grades are in.
     * @return The parsed [grades][Subject.Grades].
     */
    private fun parseSubjectGrades(gradesColumnEle: Element, subjectName: Name): Grades
    {
        val subjectGradesBuilder = Grades.builder()

        /* All the elements in the main content column. (either grade or subject part) */
        val columnContentEles = gradesColumnEle.selectFirstOrThrow("td").children()

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

        val notifications = emptyMutableLinkedList<Behaviour.Notification>()

        for (notificationEle in notificationEles)
        {
            /* The element of the icon (tick or cross) */
            val iconEle = notificationEle.selectFirstOrThrow(".sprite-icon-16")

            /* Choosing type based on it's icon. (tick = good; cross = bad) */
            val type = if (iconEle.classNames().contains("sprite-icon-tick-16"))
                Behaviour.NotificationType.GOOD
            else
                Behaviour.NotificationType.BAD

            val message = notificationEle.selectFirstOrThrow(".label").text()

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
        val valueChar = gradeEle.selectFirstOrThrow(".value").text()[0]
        val small = gradeEle.classNames().contains("scoreSmall")

        val teacherShort = gradeEle.selectFirstOrThrow(".employee").text()

        /* The title attribute of the grade element, which contains all the details. (description, date and teacher) */
        val titleAttr = gradeEle.attr("title")

        val detailsMatch = GRADE_DETAILS_REGEX.find(titleAttr) ?: return Grade(valueChar, small)

        /* Just description is optional, the rest is always there. */
        val description = detailsMatch.groups[GradeDetailsRegexGroups.DESCRIPTION]?.value
        val receiveDate = detailsMatch.groups[GradeDetailsRegexGroups.DATE]!!.value.let { LocalDate.parse(it, RECEIVE_DATE_FORMATTER) }
        val teacherFull = detailsMatch.groups[GradeDetailsRegexGroups.TEACHER]!!.value

        val teacherName = Name(teacherFull, teacherShort)

        return Grade(valueChar, small, subjectName, teacherName, description, receiveDate)
    }

    /**
     * Converts a [String] in "`name (shortname)`" format to a [Name] object.
     *
     * @return The [Name] instance.
     */
    private fun parseSubjectName(subjectNameStr: String): Name
    {
        val subjectNameMatch = SUBJECT_NAME_REGEX.find(subjectNameStr)!!

        val full = subjectNameMatch.groups[SubjectNameRegexGroups.FULL]!!.value
        val short = subjectNameMatch.groups[SubjectNameRegexGroups.SHORT]?.value

        return Name(full, short)
    }

    private fun parseSelectedSchoolYearHalf(document: Document): SchoolYearHalf
    {
        val selectedSchoolYearHalfEle = HtmlCommonParser.getSelectSelectedValue(document, "schoolYearHalfId")
            ?: throw HtmlElementNotFoundException.bySelector("#schoolYearHalfId")

        return getSchoolYearHalfByName(selectedSchoolYearHalfEle.text())
    }

    private fun getSchoolYearHalfByName(name: String): SchoolYearHalf
    {
        return when (name)
        {
            "1. pololetí" -> SchoolYearHalf.FIRST
            "2. pololetí" -> SchoolYearHalf.SECOND
            else          -> throw IllegalArgumentException("Unknown SchoolYearHalfName: $name")
        }
    }

    /**
     * The format grades' receive date is in.
     */
    private val RECEIVE_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    /**
     * Matches the [Grade]'s HTML element title. Match contains capturing groups listed in [GradeDetailsRegexGroups].
     */
    private val GRADE_DETAILS_REGEX = Regex("""
     (?:(?<${GradeDetailsRegexGroups.DESCRIPTION}>.*) )?\((?<${GradeDetailsRegexGroups.DATE}>\d{2}\.\d{2}\.\d{4}), (?<${GradeDetailsRegexGroups.TEACHER}>.*)\)${'$'}"""
                                                    .trimIndent(), RegexOption.DOT_MATCHES_ALL)

    /**
     * Contains names of regex capture groups inside [GRADE_DETAILS_REGEX].
     */
    object GradeDetailsRegexGroups
    {
        const val DESCRIPTION = "description"
        const val DATE = "date"
        const val TEACHER = "teacher"
    }

    /**
     * Matches the whole name of a subject. Match contains capturing groups listed in [SubjectNameRegexGroups].
     */
    private val SUBJECT_NAME_REGEX = Regex("""(?<${SubjectNameRegexGroups.FULL}>.*?)(?: \((?<${SubjectNameRegexGroups.SHORT}>\w{1,4})\))?${'$'}""")

    /**
     * Contains names of regex capture groups inside [GRADE_DETAILS_REGEX].
     */
    object SubjectNameRegexGroups
    {
        const val FULL = "full"
        const val SHORT = "short"
    }
}