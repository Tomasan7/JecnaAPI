package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.data.TimetablePage

/**
 * Is responsible for parsing any kind of formatted [String] (eg. JSON, XML) to [TimetablePage] instance.
 */
interface TimetableParser
{
    /**
     * @throws me.tomasan7.jecnaapi.parser.ParseException When the source isn't in correct format.
     */
    fun parse(source: String): TimetablePage
}