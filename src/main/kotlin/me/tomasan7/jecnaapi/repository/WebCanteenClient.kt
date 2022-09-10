package me.tomasan7.jecnaapi.repository

import io.ktor.http.*
import me.tomasan7.jecnaapi.data.canteen.MenuItem
import me.tomasan7.jecnaapi.parser.parsers.HtmlCanteenParserImpl
import me.tomasan7.jecnaapi.web.ICanteenWebClient

class WebCanteenClient(private val webClient: ICanteenWebClient) : CanteenClient
{
    private val parser = HtmlCanteenParserImpl()

    override suspend fun getMenu() = parser.parse(webClient.queryStringBody(WEB_PATH))

    override suspend fun order(menuItem: MenuItem) = webClient.query("/faces/secured/" + menuItem.orderURL).status == HttpStatusCode.OK


    companion object
    {
        private const val WEB_PATH = "/faces/secured/mobile.jsp"
    }
}