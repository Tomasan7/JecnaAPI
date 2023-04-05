package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.data.grade.GradesPage
import me.tomasan7.jecnaapi.parser.ParseException

/**
 * Is responsible for parsing HTML source code in [String] to [GradesPage] instance.
 */
internal interface HtmlGradesPageParser
{
    /**
     * @throws ParseException When the HTML source isn't in correct format.
     */
    fun parse(html: String): GradesPage
}
