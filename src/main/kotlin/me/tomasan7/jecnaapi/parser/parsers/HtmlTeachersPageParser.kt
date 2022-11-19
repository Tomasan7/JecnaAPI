package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.parser.ParseException
import me.tomasan7.jecnaapi.data.schoolStaff.TeachersPage

/**
 * Is responsible for parsing HTML source code in [String] to [TeachersPage] instance.
 */
interface HtmlTeachersPageParser
{
    /**
     * @throws ParseException When the HTML source isn't in correct format.
     */
    fun parse(html: String): TeachersPage
}