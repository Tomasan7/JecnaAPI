package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.data.timetable.TimetablePage
import me.tomasan7.jecnaapi.parser.ParseException

/**
 * Is responsible for parsing HTML source code in [String] to [TimetablePage] instance.
 */
internal interface HtmlTimetablePageParser
{
    /**
     * @throws ParseException When the HTML source isn't in correct format.
     */
    fun parse(html: String): TimetablePage
}