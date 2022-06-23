package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.data.grade.GradesPage

/**
 * Is responsible for parsing HTML source code in [String] to [GradesPage] instance.
 */
interface HtmlGradesPageParser
{
    /**
     * @throws me.tomasan7.jecnaapi.parser.ParseException When the HTML source isn't in correct format.
     */
    fun parse(html: String): GradesPage
}