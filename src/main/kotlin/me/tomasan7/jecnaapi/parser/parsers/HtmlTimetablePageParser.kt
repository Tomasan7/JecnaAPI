package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.data.timetable.TimetablePage

/**
 * Is responsible for parsing HTML source code in [String] to [TimetablePage] instance.
 */
interface HtmlTimetablePageParser
{
    /**
     * @throws me.tomasan7.jecnaapi.parser.ParseException When the HTML source isn't in correct format.
     */
    fun parse(html: String): TimetablePage
}