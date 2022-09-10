package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.data.canteen.DayMenu
import me.tomasan7.jecnaapi.data.canteen.Menu
import me.tomasan7.jecnaapi.data.canteen.MenuItem
import me.tomasan7.jecnaapi.data.grade.GradesPage
import me.tomasan7.jecnaapi.parser.ParseException
import me.tomasan7.jecnaapi.util.emptyMutableLinkedList
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HtmlCanteenParserImpl : HtmlCanteenParser
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
        val foodName = foodNameEle.ownText()

        if (foodName.isBlank())
            return null

        val allergensText = foodNameEle.selectFirst(".textGrey")!!.text()
        val allergens = allergensText.substring(1, allergensText.length - 1).split(", ")

        val onclick = orderButtonEle!!.attr("onclick")

        return MenuItem(
            foodName,
            allergens,
            /* Substring to remove the " Kč" suffix. */
            orderButtonEle.selectFirst(".important.warning.button-link-align")!!.text().let { it.substring(0, it.length - 3) }.toFloat(),
            orderButtonEle.hasClass("enabled"),
            orderButtonEle.hasClass("ordered"),
            onclick.substring(90, onclick.length - 29)
        )
    }

    companion object
    {
        private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    }
}