package me.tomasan7.jecnaapi

import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch
import me.tomasan7.jecnaapi.data.canteen.*
import me.tomasan7.jecnaapi.parser.ParseException
import me.tomasan7.jecnaapi.parser.parsers.HtmlCanteenParser
import me.tomasan7.jecnaapi.parser.parsers.HtmlCanteenParserImpl
import me.tomasan7.jecnaapi.parser.parsers.selectFirstOrThrow
import me.tomasan7.jecnaapi.web.Auth
import me.tomasan7.jecnaapi.web.canteen.ICanteenWebClient
import org.jsoup.Jsoup
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * A client to read and order menus.
 */
class CanteenClient
{
    private val webClient = ICanteenWebClient()
    private val canteenParser: HtmlCanteenParser = HtmlCanteenParserImpl

    private var lastTime = 0L

    suspend fun login(username: String, password: String) = login(Auth(username, password))

    suspend fun login(auth: Auth) = webClient.login(auth)

    suspend fun logout() = webClient.logout()

    suspend fun isLoggedIn() = webClient.isLoggedIn()

    suspend fun getMenuPage() = canteenParser.parse(webClient.queryStringBody(WEB_PATH))

    fun getMenuAsync(days: Iterable<LocalDate>): Flow<DayMenu> = channelFlow {
        for (day in days)
            launch {
                val dayMenu = getDayMenu(day)
                send(dayMenu)
            }
    }

    // TODO: Make it apparent that this function is making a request to the server.
    suspend fun getDayMenu(day: LocalDate): DayMenu
    {
        val dayMenuHtml = webClient.queryStringBody(
            path = "faces/secured/db/dbJidelnicekOnDayView.jsp",
            parameters = parametersOf("day", DAY_MENU_DAY_FORMATTER.format(day))
        )
        return canteenParser.parseDayMenu(dayMenuHtml)
    }

    // TODO: Make it apparent that this function is making a request to the server.
    suspend fun getCredit(): Float
    {
        val html = webClient.queryStringBody("faces/secured/main.jsp")
        val creditEle = Jsoup.parse(html).selectFirstOrThrow("#Kredit")
        return canteenParser.parseCreditText(creditEle.text())
    }

    /**
     * Orders the [menuItem].
     *
     * @param menuItem The [MenuItem] to order.
     * @return Either new credit or null, if something went wrong.
     */
    suspend fun order(menuItem: MenuItem): Float?
    {
        if (!menuItem.isEnabled)
            return null

        val finalMenuItem = if (lastTime != 0L)
            menuItem.updated(lastTime)
        else
            menuItem

        val (successful, response) = ajaxOrder(finalMenuItem.orderPath)

        if (!successful)
            return null

        return try
        {
            canteenParser.parseOrderResponse(response).credit
        }
        catch (ignored: ParseException) { null }
    }

    suspend fun putOnExchange(menuItem: MenuItem) = menuItem.putOnExchangePath?.let { ajaxOrder(it).first } ?: false

    private suspend fun ajaxOrder(url: String): Pair<Boolean, String>
    {
        val response = webClient.queryStringBody("faces/secured/$url")

        /* Same check as on the official website. */
        if (response.contains("error"))
            return false to response

        val orderResponse = canteenParser.parseOrderResponse(response)
        lastTime = orderResponse.time

        return true to response
    }

    /**
     * Returns a [MenuItem] with updated time in the [MenuItem.orderPath] and possibly [MenuItem.putOnExchangePath].
     */
    private fun MenuItem.updated(time: Long): MenuItem
    {
        val newOrderPath = orderPath.replace(TIME_REPLACE_REGEX, time.toString())
        val newPutOnExchangePath = putOnExchangePath?.replace(TIME_REPLACE_REGEX, time.toString())
        return copy(orderPath = newOrderPath, putOnExchangePath = newPutOnExchangePath)
    }

    /**
     * Makes a request to the provided path. Responses may vary depending on whether user is logged in or not.
     *
     * @param path Relative path from the domain. Must include first slash.
     * @param parameters HTTP parameters, which will be sent URL encoded.
     * @return The [HttpResponse].
     */
    suspend fun query(path: String, parameters: Parameters? = null) = webClient.query(path, parameters)

    /**
     * Makes a request to the provided path. Responses may vary depending on whether user is logged in or not.
     *
     * @param path Relative path from the domain. Must include first slash.
     * @param parameters HTTP parameters, which will be sent URL encoded.
     * @return The HTTP response's body as [String].
     */
    suspend fun queryStringBody(path: String, parameters: Parameters? = null) =
        webClient.queryStringBody(path, parameters)

    companion object
    {
        private const val WEB_PATH = "faces/secured/mobile.jsp"
        private val TIME_REPLACE_REGEX = Regex("""(?<=time=)\d{13}""")
        private val DAY_MENU_DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }
}
