package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.data.canteen.*
import me.tomasan7.jecnaapi.parser.ParseException
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal object HtmlCanteenParserImpl : HtmlCanteenParser
{
    override fun parse(html: String): MenuPage
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

           val credit = parseCredit(document.selectFirst("#Kredit")!!.text())

            return MenuPage(menuBuilder.build(), credit)
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

    override fun parseOrderResponse(orderResponseHtml: String): OrderResponse
    {
        val document = Jsoup.parse(orderResponseHtml)

        val creditEle = document.selectFirst("#Kredit")!!
        val timeEle = document.selectFirst("#time")!!

        val credit = parseCredit(creditEle.text())
        val time = timeEle.text().toLong()

        return OrderResponse(credit, time)
    }

    private fun parseCredit(creditEleText: String): Float
    {
        /* Substring to remove the " Kč" suffix. */
        /* Comma replaced with dot to make conversion to float possible. */
        /* Space removed, because there might be one between the thousand and so digits. */
        return creditEleText.let { it.substring(0, it.length - 3) }.replace(',', '.').replace(" ", "").toFloat()
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

        val itemDescription = if (itemDescriptionStr.isNotEmpty())
        {
            val itemDescriptionMatch = ITEM_DESCRIPTION_REGEX.find(itemDescriptionStr)

            val soup = itemDescriptionMatch?.groups?.get(ItemDescriptionRegexGroups.SOUP)?.value
            val rest = itemDescriptionMatch?.groups?.get(ItemDescriptionRegexGroups.REST)?.value

            rest?.let { ItemDescription(soup, it) }
        }
        else null

        val allergensText = foodNameEle.selectFirst(".textGrey")?.text()
        val allergens = allergensText?.substring(1, allergensText.length - 1)?.split(", ")

        val onclick = orderButtonEle!!.attr("onclick")

        val putOnExchangeButtonEle = menuItemEle.getElementById("icons14712")?.selectFirst("a")
        val putOnExchangeOnClick = putOnExchangeButtonEle?.attr("onclick")

        return MenuItem(
            description = itemDescription,
            allergens = allergens,
            /* Substring to remove the " Kč" suffix. */
            price = orderButtonEle.selectFirst(".important.warning.button-link-align")!!.text().let { it.substring(0, it.length - 3) }.toFloat(),
            enabled = !orderButtonEle.hasClass("disabled"),
            /* Query for the check mark in the button ele. */
            ordered = orderButtonEle.selectFirst(".fa.fa-check.fa-2x") != null,
            orderPath = onclick.substring(90, onclick.length - 29), // ajaxOrder(this, 'db/dbProcessOrder.jsp?time=undefined&token=Ceuj7lb%2F636XwFz%2FkMJ0t5Ou7RBy9Xz5pPvdubjucP4%3D&ID=1&day=2022-11-25&type=make&week=&terminal=false&keyboard=false&printer=false', '2022-11-25', 'enabled');
            putOnExchangePath = putOnExchangeOnClick?.substring(17, putOnExchangeOnClick.length - 28) // ajaxOrder(this, 'db/dbProcessOrder.jsp?time=undefined&token=fSngzd9eNbXDfIwk7VRTSQ4dzDQ2dwzLy8eaR5HG%2FG4%3D&ID=5029311&day=2022-11-23&type=minusburza&week=&terminal=false&keyboard=false&printer=false', '2022-11-23', 'allowed');
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