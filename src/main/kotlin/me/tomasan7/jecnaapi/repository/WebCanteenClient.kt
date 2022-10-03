package me.tomasan7.jecnaapi.repository

import io.ktor.http.*
import me.tomasan7.jecnaapi.data.canteen.MenuItem
import me.tomasan7.jecnaapi.data.canteen.MenuPage
import me.tomasan7.jecnaapi.parser.parsers.HtmlCanteenParser
import me.tomasan7.jecnaapi.parser.parsers.HtmlCanteenParserImpl
import me.tomasan7.jecnaapi.web.ICanteenWebClient
import org.jsoup.Jsoup
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class WebCanteenClient(
    private val webClient: ICanteenWebClient,
    private val canteenParser: HtmlCanteenParser = HtmlCanteenParserImpl
) : CanteenClient
{
    override suspend fun getMenuPage() = canteenParser.parse(webClient.queryStringBody(WEB_PATH))

    override suspend fun order(menuItem: MenuItem): Boolean
    {
        if (!menuItem.enabled)
            return false

        val response = webClient.queryStringBody("/faces/secured/" + menuItem.orderURL)

        /* Server responds with HTML, when everything went right or JSON error if something went wrong. */
        return response.startsWith('<')
    }

    override suspend fun order(menuItem: MenuItem, dayMenuDay: LocalDate, menuPage: MenuPage): Boolean
    {
        if (!menuItem.enabled)
            return false

        val response = webClient.queryStringBody("/faces/secured/" + menuItem.orderURL)

        /* Server responds with HTML, when everything went right or JSON error if something went wrong. */
        if (!response.startsWith('<'))
            return false

        /* The day string formatted as the server accepts it. */
        val dayStr = dayMenuDay.format(DAY_MENU_DAY_FORMATTER)
        val newDayMenuHtml = webClient.queryStringBody("/faces/secured/db/dbJidelnicekOnDayView.jsp", Parameters.build { append("day", dayStr) })
        val newDayMenu = canteenParser.parseDayMenu(newDayMenuHtml)

        menuPage.menu.replace(dayMenuDay, newDayMenu)

        val responseDocument = Jsoup.parse(response)
        val newTime = responseDocument.selectFirst("#time")?.text()?.toLong() ?: return false
        /* Substring to remove the " Kč" suffix. */
        val newCredit = responseDocument.selectFirst("#Kredit")!!.text().replace(',', '.').replace(" ", "").let { it.substring(0, it.length - 3) }.toFloat()

        menuPage.credit = newCredit

        val menuItems = menuPage.menu.dayMenus.flatMap { it.items }

        /* Updating time on all menu items. */
        menuItems.forEach {
            it.orderURL = it.orderURL.replace(TIME_REPLACE_REGEX, newTime.toString())
        }

        return true
    }

    companion object
    {
        private const val WEB_PATH = "/faces/secured/mobile.jsp"
        private val TIME_REPLACE_REGEX = Regex("""(?<=time=)\d{13}""")
        private val DAY_MENU_DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }
}