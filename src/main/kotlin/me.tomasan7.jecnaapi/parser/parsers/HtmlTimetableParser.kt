package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.data.Lesson
import me.tomasan7.jecnaapi.data.LessonPeriod
import me.tomasan7.jecnaapi.data.LessonSpot
import me.tomasan7.jecnaapi.data.Timetable
import me.tomasan7.jecnaapi.parser.ParseException
import org.jsoup.Jsoup

/**
 * Parses correct HTML to [Timetable] instance.
 */
class HtmlTimetableParser : TimetableParser
{
    override fun parse(source: String): Timetable
    {
        try
        {
            val timetableBuilder = Timetable.builder()

            val document = Jsoup.parse(source)

            /* All the rows (tr) in the grades table. */
            val rowEles = document.select("table.timetable > tbody > tr")

            /* The row (tr) containing all the LessonPeriods details.
			 * Each LessonPeriod is a 'th' with class 'period'. */
            val lessonPeriodsEle = rowEles[0]

            /* All the LessonPeriod elements.
			 * Each element has an inner text with the index of the hour
			 * and a span (with class 'time') containing the time period. */
            val lessonPeriodEles = lessonPeriodsEle.select("th.period")

            /* Add all the LessonPeriods to the timetable. */
            lessonPeriodEles.forEach {
                timetableBuilder.addLessonPeriod(LessonPeriod.fromString(it.selectFirst(".time")!!.text()))
            }

            /* Removes the row with the LessonPeriods, so it leaves all the subjects. */
            rowEles.removeAt(0)

            for (rowEle in rowEles)
            {
                val day = rowEle.selectFirst(".day")!!.text()

                /* All the LessonSpots in this day. */
                val lessonSpotEles = rowEle.select("td")

                for (lessonSpotEle in lessonSpotEles)
                {
                    /* Skip if the lesson spot is empty, this leaves the lesson variable to null -> indicating no lesson. */
                    if (lessonSpotEle.hasClass("empty"))
                    {
                        timetableBuilder.addLesson(day, null)
                        continue
                    }

                    /* All the lessons in the lesson spot. */
                    val lessonEles = lessonSpotEle.getElementsByTag("div")

                    val lessons = ArrayList<Lesson>()

                    for (lessonEle in lessonEles)
                    {
                        var group = 0
                        val groupEle = lessonEle.selectFirst(".group")
                        if (groupEle != null)
                            group = groupEle.text().split("/")[0].toInt()

                        lessons.add(Lesson(
                            lessonEle.selectFirst(".subject")!!.attr("title"),
                            lessonEle.selectFirst(".subject")!!.text(),
                            lessonEle.selectFirst(".employee")!!.attr("title"),
                            lessonEle.selectFirst(".employee")!!.text(),
                            lessonEle.selectFirst(".room")!!.text(),
                            group
                        ))
                    }

                    timetableBuilder.addLessonSpot(day, LessonSpot(lessons))
                }
            }

            return timetableBuilder.build()
        }
        catch (e: Exception)
        {
            throw ParseException(e)
        }
    }
}