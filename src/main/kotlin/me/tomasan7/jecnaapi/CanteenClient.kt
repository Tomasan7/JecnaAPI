package me.tomasan7.jecnaapi

import io.ktor.client.statement.*
import io.ktor.http.*
import me.tomasan7.jecnaapi.data.canteen.*
import me.tomasan7.jecnaapi.parser.parsers.HtmlCanteenParser
import me.tomasan7.jecnaapi.parser.parsers.HtmlCanteenParserImpl
import me.tomasan7.jecnaapi.web.Auth
import me.tomasan7.jecnaapi.web.ICanteenWebClient
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
        if (!menuItem.enabled)
            return false

        val response = webClient.queryStringBody("/faces/secured/" + menuItem.orderPath)

        /* Server responds with HTML, when everything went right or JSON error if something went wrong. */
        return response.startsWith('<')
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
        if (!menuItem.enabled)
            return false

        val response = webClient.queryStringBody("/faces/secured/" + menuItem.orderPath)

        /* Server responds with HTML, when everything went right or JSON error if something went wrong. */
        if (!response.startsWith('<'))
            return false

        /* The day string formatted as the server accepts it. */
        val dayStr = dayMenuDay.format(DAY_MENU_DAY_FORMATTER)
        val newDayMenuHtml = webClient.queryStringBody("/faces/secured/db/dbJidelnicekOnDayView.jsp", Parameters.build { append("day", dayStr) })
        val newDayMenu = canteenParser.parseDayMenu(newDayMenuHtml)

        menuPage.menu.replace(dayMenuDay, newDayMenu)

       val orderResponse = canteenParser.parseOrderResponse(response)

        menuPage.credit = orderResponse.credit

        val menuItems = menuPage.menu.dayMenus.flatMap { it.items }

        /* Updating time on all menu items. */
        menuItems.forEach { it.updateTime(orderResponse.time) }

        return true
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
    suspend fun order(menuItem: MenuItem, menuPage: MenuPage) = order(menuItem, menuPage.menu.dayMenus.find { it.items.contains(menuItem) }!!.day, menuPage)

    suspend fun putOnExchange(menuItem: MenuItem, dayMenuDay: LocalDate, menuPage: MenuPage): Boolean
    {
        val response = webClient.queryStringBody("/faces/secured/" + menuItem.putOnExchangePath)

        /* Server responds with HTML, when everything went right or JSON error if something went wrong. */
        if (!response.startsWith('<'))
            return false

        /* The day string formatted as the server accepts it. */
        val dayStr = dayMenuDay.format(DAY_MENU_DAY_FORMATTER)
        val newDayMenuHtml = webClient.queryStringBody("/faces/secured/db/dbJidelnicekOnDayView.jsp", Parameters.build { append("day", dayStr) })
        val newDayMenu = canteenParser.parseDayMenu(newDayMenuHtml)

        menuPage.menu.replace(dayMenuDay, newDayMenu)

        val orderResponse = canteenParser.parseOrderResponse(response)

        menuPage.credit = orderResponse.credit

        val menuItems = menuPage.menu.dayMenus.flatMap { it.items }

        /* Updating time on all menu items. */
        menuItems.forEach { it.updateTime(orderResponse.time) }

        return true
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