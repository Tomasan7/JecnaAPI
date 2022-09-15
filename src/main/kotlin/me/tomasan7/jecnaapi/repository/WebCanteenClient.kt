package me.tomasan7.jecnaapi.repository

import io.ktor.http.*
import me.tomasan7.jecnaapi.data.canteen.MenuItem
import me.tomasan7.jecnaapi.parser.parsers.HtmlCanteenParser
import me.tomasan7.jecnaapi.web.ICanteenWebClient

class WebCanteenClient(
    private val webClient: ICanteenWebClient,
    private val canteenParser: HtmlCanteenParser
) : CanteenClient
{
    override suspend fun getMenu() = canteenParser.parse(webClient.queryStringBody(WEB_PATH))

    override suspend fun order(menuItem: MenuItem) = webClient.query("/faces/secured/" + menuItem.orderURL).status == HttpStatusCode.OK

    companion object
    {
        private const val WEB_PATH = "/faces/secured/mobile.jsp"
    }
}