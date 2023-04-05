package me.tomasan7.jecnaapi

import io.ktor.client.statement.*
import io.ktor.http.*
import me.tomasan7.jecnaapi.data.canteen.*
import me.tomasan7.jecnaapi.parser.parsers.HtmlCanteenParser
import me.tomasan7.jecnaapi.parser.parsers.HtmlCanteenParserImpl
import me.tomasan7.jecnaapi.web.Auth
import me.tomasan7.jecnaapi.web.canteen.ICanteenWebClient
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * A client to read and order menus.
 */
class CanteenClient
{
    private val webClient = ICanteenWebClient()

    private val canteenParser: HtmlCanteenParser = HtmlCanteenParserImpl

    suspend fun login(username: String, password: String) = login(Auth(username, password))

    suspend fun login(auth: Auth) = webClient.login(auth)

    suspend fun logout() = webClient.logout()

    suspend fun isLoggedIn() = webClient.isLoggedIn()

    suspend fun getMenuPage() = canteenParser.parse(webClient.queryStringBody(WEB_PATH))

    /**
     * Orders the [menuItem].
     *
     * @param menuItem The [MenuItem] to order.
     * @return Whether the order was successful or not.
     */
    suspend fun order(menuItem: MenuItem): Boolean
    {
        if (!menuItem.isEnabled)
            return false

        val response = webClient.queryStringBody("/faces/secured/" + menuItem.orderPath)

        /* Same check as on the original website. */
        return !response.contains("error")
    }

    /**
     * Orders the [menuItem].
     * And updates whole [MenuPage] accordingly.
     *
     * @param menuItem The [MenuItem] to order.
     * @param dayMenuDay the [day][LocalDate] of the [DayMenu] the [menuItem] is in.
     * @param menuPage The [Menu] the [menuItem] is in.
     * @return Whether the order was successful or not.
     */
    suspend fun order(menuItem: MenuItem, dayMenuDay: LocalDate, menuPage: MenuPage): Boolean
    {
        if (!menuItem.isEnabled)
            return false

        return ajaxOrder(menuItem.orderPath, dayMenuDay, menuPage)
    }

    /**
     * Orders the [menuItem].
     * And updates whole [MenuPage] accordingly.
     *
     * @param menuItem The [MenuItem] to order.
     * @param dayMenuDay the [day][LocalDate] of the [DayMenu] the [menuItem] is in.
     * @param menuPage The [Menu] the [menuItem] is in.
     * @return Whether the order was successful or not.
     */
    suspend fun order(menuItem: MenuItem, dayMenu: DayMenu, menuPage: MenuPage) = order(menuItem, dayMenu.day, menuPage)

    /**
     * Orders the [menuItem].
     * And updates whole [MenuPage] accordingly.
     * Use [order] with [dayMenuDay][LocalDate] parameter, if you have it.
     * This function finds the [day][LocalDate] of the [menuItem] in the [menuPage].
     *
     * @param menuItem The [MenuItem] to order.
     * @param menuPage The [MenuPage] the [menuItem] is in.
     * @return Whether the order was successful or not.
     */
    suspend fun order(menuItem: MenuItem, menuPage: MenuPage) =
        order(menuItem, menuPage.menu.dayMenus.find { it.items.contains(menuItem) }!!.day, menuPage)

    suspend fun putOnExchange(menuItem: MenuItem, dayMenuDay: LocalDate, menuPage: MenuPage) =
        menuItem.putOnExchangePath?.let { ajaxOrder(it, dayMenuDay, menuPage) } ?: false

    suspend fun putOnExchange(menuItem: MenuItem, menuPage: MenuPage) =
        putOnExchange(menuItem, menuPage.menu.dayMenus.find { it.items.contains(menuItem) }!!.day, menuPage)

    private suspend fun ajaxOrder(url: String, dayMenuDay: LocalDate, menuPage: MenuPage): Boolean
    {
        val ajaxOrderRequestResult = ajaxOrderRequest(url)

        if (!ajaxOrderRequestResult.first)
            return false

        requestAndUpdateDayMenu(dayMenuDay, menuPage.menu)

        val responseStr = ajaxOrderRequestResult.second
        val orderResponse = canteenParser.parseOrderResponse(responseStr)

        menuPage.update(orderResponse)

        return true
    }

    private suspend fun ajaxOrderRequest(url: String): Pair<Boolean, String>
    {
        val response = webClient.queryStringBody("/faces/secured/$url")

        /* Same check as on the official website. */
        if (response.contains("error"))
            return false to response

        return true to response
    }

    private fun MenuPage.update(orderResponse: OrderResponse)
    {
        credit = orderResponse.credit

        /* Updating time on all menu items. */
        menu.dayMenus.forEach { dayMenu -> dayMenu.items.forEach{ it.updateTime(orderResponse.time) } }
    }

    private suspend fun requestAndUpdateDayMenu(dayMenuDay: LocalDate, menu: Menu)
    {
        val newDayMenu = requestDayMenu(dayMenuDay)
        menu.replace(dayMenuDay, newDayMenu)
    }

    private suspend fun requestDayMenu(dayMenuDay: LocalDate): DayMenu
    {
        /* The day string formatted as the server accepts it. */
        val dayStr = dayMenuDay.format(DAY_MENU_DAY_FORMATTER)
        val newDayMenuHtml = webClient.queryStringBody(
            path = "/faces/secured/db/dbJidelnicekOnDayView.jsp",
            parameters = Parameters.build { append("day", dayStr) }
        )

        return canteenParser.parseDayMenu(newDayMenuHtml)
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
    suspend fun queryStringBody(path: String, parameters: Parameters? = null) = webClient.queryStringBody(path, parameters)

    companion object
    {
        private const val WEB_PATH = "/faces/secured/mobile.jsp"
        private val DAY_MENU_DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }
}
