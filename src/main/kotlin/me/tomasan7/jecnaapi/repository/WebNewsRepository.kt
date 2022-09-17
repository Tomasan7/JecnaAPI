package me.tomasan7.jecnaapi.repository

import me.tomasan7.jecnaapi.parser.parsers.HtmlNewsPageParser
import me.tomasan7.jecnaapi.web.JecnaWebClient

/**
 * Retrieves [NewsRepository] from the Ječná web.
 */
class WebNewsRepository(
    private val webClient: JecnaWebClient,
    private val newsParser: HtmlNewsPageParser
) : NewsRepository
{
    override suspend fun queryNewsPage() = newsParser.parse(webClient.queryStringBody(WEB_PATH))

    companion object
    {
        private const val WEB_PATH = "/"
    }
}