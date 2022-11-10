package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.data.article.NewsPage
import me.tomasan7.jecnaapi.parser.ParseException

/**
 * Is responsible for parsing HTML source code in [String] to [NewsPage] instance.
 */
internal interface HtmlNewsPageParser
{
    /**
     * @throws ParseException When the HTML source isn't in correct format.
     */
    fun parse(html: String): NewsPage
}