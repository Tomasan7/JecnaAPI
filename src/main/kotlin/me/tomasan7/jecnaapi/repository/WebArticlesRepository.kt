package me.tomasan7.jecnaapi.repository

import me.tomasan7.jecnaapi.parser.parsers.HtmlNewsPageParserImpl
import me.tomasan7.jecnaapi.web.JecnaWebClient

/**
 * Retrieves [ArticlesRepository] from the Ječná web.
 */
class WebArticlesRepository(private val webClient: JecnaWebClient) : ArticlesRepository
{
    private val articlesParser = HtmlNewsPageParserImpl()

    override suspend fun queryArticlesPage() = articlesParser.parse(webClient.queryStringBody(WEB_PATH))


    companion object
    {
        private const val WEB_PATH = "/"
    }
}