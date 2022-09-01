package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.data.ArticlesPage

/**
 * Is responsible for parsing HTML source code in [String] to [ArticlesPage] instance.
 */
interface HtmlArticlesPageParser
{
    /**
     * @throws me.tomasan7.jecnaapi.parser.ParseException When the HTML source isn't in correct format.
     */
    fun parse(html: String): ArticlesPage
}