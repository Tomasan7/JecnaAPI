package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.data.canteen.DayMenu
import me.tomasan7.jecnaapi.data.canteen.ItemDescription
import me.tomasan7.jecnaapi.data.canteen.Menu
import me.tomasan7.jecnaapi.data.canteen.MenuItem
import me.tomasan7.jecnaapi.parser.ParseException
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object HtmlCanteenParserImpl : HtmlCanteenParser
{
    override fun parse(html: String): Menu
    {
        try
        {
            val menuBuilder = Menu.builder()

            val document = Jsoup.parse(html)

            val formEles = document.select("#mainContext > table > tbody > tr > td > form")

            for (formEle in formEles)
            {
                val dayMenu = parseDayMenu(formEle)
                menuBuilder.addDayMenu(dayMenu.day, dayMenu)
            }

            return menuBuilder.build()
        }
        catch (e: Exception)
        {
            throw ParseException(e)
        }
    }

    override fun parseDayMenu(html: String): DayMenu
    {
        val element = Jsoup.parse(html).selectFirst("body")!!
        return parseDayMenu(element)
    }

    private fun parseDayMenu(dayMenuEle: Element): DayMenu
    {
        val dayStr = dayMenuEle.selectFirst("div > strong > .important")!!.text()
        val day = LocalDate.parse(dayStr, DATE_FORMAT)

        val dayMenuBuilder = DayMenu.builder(day)

        val menuItemEles = dayMenuEle.select(".orderContent > div > div")

        for (menuItemEle in menuItemEles)
            parseMenuItem(menuItemEle)?.let { dayMenuBuilder.addMenuItem(it) }

        return dayMenuBuilder.build()
    }

    private fun parseMenuItem(menuItemEle: Element): MenuItem?
    {
        val eles = menuItemEle.select("> span > span")
        val orderButtonEle = eles[0].selectFirst("a")
        val foodNameEle = eles[1]
        val itemDescriptionStr = foodNameEle.ownText()
        val itemDescriptionMatch = ITEM_DESCRIPTION_REGEX.find(itemDescriptionStr) ?: return null

        val itemDescriptionSoup = itemDescriptionMatch.groups[ItemDescriptionRegexGroups.SOUP]!!.value
        val itemDescriptionRest = itemDescriptionMatch.groups[ItemDescriptionRegexGroups.REST]!!.value

        val allergensText = foodNameEle.selectFirst(".textGrey")!!.text()
        val allergens = allergensText.substring(1, allergensText.length - 1).split(", ")

        val onclick = orderButtonEle!!.attr("onclick")

        return MenuItem(
            description = ItemDescription(itemDescriptionSoup, itemDescriptionRest),
            allergens = allergens,
            /* Substring to remove the " Kč" suffix. */
            price = orderButtonEle.selectFirst(".important.warning.button-link-align")!!.text().let { it.substring(0, it.length - 3) }.toFloat(),
            enabled = !orderButtonEle.hasClass("disabled"),
            ordered = orderButtonEle.hasClass("ordered"),
            orderURL = onclick.substring(90, onclick.length - 29)
        )
    }

    private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    /**
     * Matches the whole item description. Match contains capturing groups listed in [ItemDescriptionRegexGroups].
     */
    private val ITEM_DESCRIPTION_REGEX = Regex("""^(?<${ItemDescriptionRegexGroups.SOUP}>.*?) , ;(?<${ItemDescriptionRegexGroups.REST}>.*)""")

    /**
     * Contains names of regex capture groups inside [ITEM_DESCRIPTION_REGEX].
     */
    object ItemDescriptionRegexGroups
    {
        const val SOUP = "soup"
        const val REST = "rest"
    }
}