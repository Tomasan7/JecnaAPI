package me.tomasan7.jecnaapi.repository

import me.tomasan7.jecnaapi.data.canteen.MenuItem
import me.tomasan7.jecnaapi.parser.parsers.HtmlCanteenParser
import me.tomasan7.jecnaapi.web.ICanteenWebClient

class WebCanteenClient(
    private val webClient: ICanteenWebClient,
    private val canteenParser: HtmlCanteenParser
) : CanteenClient
{
    override suspend fun getMenu() = canteenParser.parse(webClient.queryStringBody(WEB_PATH))

    override suspend fun order(menuItem: MenuItem): Boolean
    {
        val response = webClient.queryStringBody("/faces/secured/" + menuItem.orderURL)

        /* Server responds with HTML, when everything went right or JSON error if something went wrong. */
        return response.startsWith('<')
    }

    companion object
    {
        private const val WEB_PATH = "/faces/secured/mobile.jsp"
    }
}