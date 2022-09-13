package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.data.article.NewsPage

/**
 * Is responsible for parsing HTML source code in [String] to [NewsPage] instance.
 */
interface HtmlNewsPageParser
{
    /**
     * @throws me.tomasan7.jecnaapi.parser.ParseException When the HTML source isn't in correct format.
     */
    fun parse(html: String): NewsPage
}