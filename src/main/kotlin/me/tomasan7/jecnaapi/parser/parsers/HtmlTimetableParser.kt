package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.data.timetable.Timetable

/** Parses correct HTML to [Timetable] instance. */
internal interface HtmlTimetableParser
{
    fun parse(html: String): Timetable
}