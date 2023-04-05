package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.data.attendance.AttendancesPage
import me.tomasan7.jecnaapi.parser.ParseException

/**
 * Is responsible for parsing HTML source code in [String] to [AttendancesPage] instance.
 */
internal interface HtmlAttendancesPageParser
{
    /**
     * @throws ParseException When the HTML source isn't in correct format.
     */
    fun parse(html: String): AttendancesPage
}
