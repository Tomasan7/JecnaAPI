package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.data.*
import me.tomasan7.jecnaapi.parser.ParseException
import me.tomasan7.jecnaapi.util.Name
import me.tomasan7.jecnaapi.util.emptyMutableLinkedList
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

/**
 * Parses correct HTML to [TimetablePage] instance.
 */
class HtmlTimetableParserImpl : HtmlTimetablePageParser
{
    override fun parse(html: String): TimetablePage
    {
        try
        {
            val timetablePageBuilder = TimetablePage.builder()

            val document = Jsoup.parse(html)

            /* All the rows (tr) in the timetable table. */
            val rowEles = document.select("table.timetable > tbody > tr")

            /* The row (tr) containing all the LessonPeriods details.
			 * Each LessonPeriod is a 'th' with class 'period'. */
            val lessonPeriodsEle = rowEles[0]

            /* All the LessonPeriod elements.
			 * Each element has an inner text with the index of the hour
			 * and a span (with class 'time') containing the time period. */
            val lessonPeriodEles = lessonPeriodsEle.select("th.period")

            /* Add all the LessonPeriods to the timetable. */
            for (lessonPeriodEle in lessonPeriodEles)
                timetablePageBuilder.addLessonPeriod(parseLessonPeriod(lessonPeriodEle))

            /* Removes the row with the LessonPeriods, so it leaves all the subjects. */
            rowEles.removeAt(0)

            for (rowEle in rowEles)
            {
                val day = rowEle.selectFirst(".day")!!.text()

                /* All the LessonSpots in this day. */
                val lessonSpotEles = rowEle.select("td")

                for (lessonSpotEle in lessonSpotEles)
                    timetablePageBuilder.addLessonSpot(day, parseLessonSpot(lessonSpotEle))
            }

            return timetablePageBuilder.build()
        }
        catch (e: Exception)
        {
            throw ParseException(e)
        }
    }

    /**
     * Parses [LessonPeriod] from it's HTML element.
     *
     * @return The parsed [LessonPeriod].
     */
    private fun parseLessonPeriod(lessonPeriodEle: Element) = LessonPeriod.fromString(lessonPeriodEle.selectFirst(".time")!!.text())

    /**
     * Parses [LessonSpot] from it's HTML element.
     *
     * @return The parsed [LessonSpot].
     */
    private fun parseLessonSpot(lessonSpotEle: Element): LessonSpot?
    {
        /* Skip if the lesson spot is empty, this leaves the lesson variable to null -> indicating no lesson. */
        if (lessonSpotEle.hasClass("empty"))
            return null

        /* All the lessons in the lesson spot. */
        val lessonEles = lessonSpotEle.getElementsByTag("div")

        val lessons = emptyMutableLinkedList<Lesson>()

        for (lessonEle in lessonEles)
            lessons.add(parseLesson(lessonEle))

        return LessonSpot(lessons)
    }

    /**
     * Parses [Lesson] from it's HTML element.
     *
     * @return The parsed [Lesson].
     */
    private fun parseLesson(lessonEle: Element): Lesson
    {
        /* Group is nullable.
        * Is null when there is only a single lesson in a lesson spot. */
        val groupEle = lessonEle.selectFirst(".group")
        val subjectEle = lessonEle.selectFirst(".subject")!!
        val employeeEle = lessonEle.selectFirst(".employee")!!

        /* Group 0 indicates, that there are no groups, thus whole class has the lesson. */
        val group = if (groupEle != null)
            groupEle.text().split("/")[0].toInt()
        else
            0

        val subjectName = Name(subjectEle.attr("title"),
                               subjectEle.text())

        val teacherName = Name(employeeEle.attr("title"),
                               employeeEle.text())

        return Lesson(
            subjectName = subjectName,
            teacherName = teacherName,
            classroom = lessonEle.selectFirst(".room")!!.text(),
            group = group
        )
    }
}