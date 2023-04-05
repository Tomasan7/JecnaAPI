package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.parser.ParseException
import me.tomasan7.jecnaapi.data.schoolStaff.Teacher

/**
 * Is responsible for parsing HTML source code in [String] to [Teacher] instance.
 */
internal interface HtmlTeacherParser
{
    /**
     * @throws ParseException When the HTML source isn't in correct format.
     */
    fun parse(html: String): Teacher
}
