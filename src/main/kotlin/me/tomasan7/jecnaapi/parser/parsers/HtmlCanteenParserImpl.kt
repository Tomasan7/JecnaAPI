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

            val credit = parseCredit(document.selectFirstOrThrow("#Kredit").text())

            return MenuPage(menuBuilder.build(), credit)
        }
        catch (e: Exception)
        {
            throw ParseException("Failed to parse canteen.", e)
        }
    }

    override fun parseDayMenu(html: String): DayMenu
    {
        val element = Jsoup.parse(html).selectFirstOrThrow("body")
        return parseDayMenu(element)
    }

    override fun parseOrderResponse(orderResponseHtml: String): OrderResponse
    {
        val document = Jsoup.parse(orderResponseHtml)

        val creditEle = document.selectFirstOrThrow("#Kredit")
        val timeEle = document.selectFirstOrThrow("#time")

        val credit = parseCredit(creditEle.text())
        val time = timeEle.text().toLong()

        return OrderResponse(credit, time)
    }

    private fun parseCredit(creditEleText: String) = creditEleText
        .trim()
        .replace(" Kč", "")
        /* Comma replaced with dot to make conversion to float possible. */
        .replace(',', '.')
        /* Space removed, because there might be one between the thousand and so digits. */
        .replace(" ", "")
        .toFloat()

    private fun parseDayMenu(dayMenuEle: Element): DayMenu
    {
        val dayStr = dayMenuEle.selectFirstOrThrow("div > strong > .important").text()
        val day = LocalDate.parse(dayStr, DATE_FORMAT)

        val dayMenuBuilder = DayMenu.builder(day)

        val menuItemEles = dayMenuEle.select(".orderContent > div > div")

        for (menuItemEle in menuItemEles)
            dayMenuBuilder.addMenuItem(parseMenuItem(menuItemEle))

        return dayMenuBuilder.build()
    }

    private fun parseMenuItem(menuItemEle: Element): MenuItem
    {
        val eles = menuItemEle.select("> span > span")
        val orderButtonEle = eles[0].selectFirstOrThrow("a", "order button")
        val foodNameEle = eles[1]
        val itemDescriptionStr = foodNameEle.ownText()
        val numberText = orderButtonEle.selectFirstOrThrow(".smallBoldTitle.button-link-align", "lunch number text")
        val number = numberText.text().replace("Oběd ", "").toInt()

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

        val onclick = orderButtonEle.attr("onclick")

        val putOnExchangeButtonEle = menuItemEle.selectFirst(".icons")?.allElements?.find { it.ownText() in arrayOf("do burzy >", "z burzy <") }
        val putOnExchangeOnClick = putOnExchangeButtonEle?.attr("onclick")

        return MenuItem(
            number = number,
            description = itemDescription,
            allergens = allergens,
            price = parseCredit(
                orderButtonEle.selectFirstOrThrow(".important.warning.button-link-align", "order price").text()
            ),
            isEnabled = !orderButtonEle.hasClass("disabled"),
            /* Query for the check mark in the button ele. */
            isOrdered = orderButtonEle.selectFirst(".fa.fa-check.fa-2x") != null,
            isInExchange = putOnExchangeButtonEle?.text()?.let { it == "z burzy <" } ?: false,
            orderPath = onclick.substring(90, onclick.length - 29),
            putOnExchangePath = putOnExchangeOnClick?.substring(17, putOnExchangeOnClick.length - 28)
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
    private object ItemDescriptionRegexGroups
    {
        const val SOUP = "soup"
        const val REST = "rest"
    }
}
