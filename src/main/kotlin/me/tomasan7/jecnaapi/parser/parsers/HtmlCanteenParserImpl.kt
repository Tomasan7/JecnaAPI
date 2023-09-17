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

            val credit = parseCreditText(document.selectFirstOrThrow("#Kredit").text())

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

        val credit = parseCreditText(creditEle.text())
        val time = timeEle.text().toLong()

        return OrderResponse(credit, time)
    }

    override fun parseCreditText(creditEleText: String) = creditEleText
        .trim()
        .replace(" Kč", "")
        /* Comma replaced with dot to make conversion to float possible. */
        .replace(',', '.')
        /* Space removed, because there might be one between the thousand and so digits. */
        .replace(" ", "")
        .toFloat()

    private fun parseDayMenu(dayMenuEle: Element): DayMenu
    {
        val dayTitle = dayMenuEle.selectFirstOrThrow(".jidelnicekTop").text()
        val dayStr = DATE_REGEX.find(dayTitle)?.value ?: throw ParseException("Failed to parse day date.")
        val day = LocalDate.parse(dayStr, DATE_FORMAT)

        val dayMenuBuilder = DayMenu.builder(day)

        val menuItemEles = dayMenuEle.select(".jidelnicekMain > .jidelnicekItem")

        for (menuItemEle in menuItemEles)
            dayMenuBuilder.addMenuItem(parseMenuItem(menuItemEle))

        return dayMenuBuilder.build()
    }

    private fun parseMenuItem(menuItemEle: Element): MenuItem
    {
        val orderButtonEle = menuItemEle.selectFirstOrThrow(".jidWrapLeft > a", "order button")
        val foodNameEle = menuItemEle.selectFirstOrThrow(".jidWrapCenter", "food name")
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

        val allergens = menuItemEle.select(".textGrey > .textGrey").map { rawText(it.attr("title")) }

        val onclick = orderButtonEle.attr("onclick")

        val putOnExchangeButtonEle = menuItemEle.selectFirst(".input-group")?.allElements?.find {
            it.ownText().matches(Regex("ks (?:z|do) burzy"))
        }
        val putOnExchangeOnClick = putOnExchangeButtonEle?.attr("onclick")

        return MenuItem(
            number = number,
            description = itemDescription,
            allergens = allergens,
            price = parseCreditText(
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

    private fun rawText(html: String) = Jsoup.parse(html).text()

    private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

    private val DATE_REGEX = Regex("""\d{2}\.\d{2}\.\d{4}""")

    /**
     * Matches the whole item description. Match contains capturing groups listed in [ItemDescriptionRegexGroups].
     */
    private val ITEM_DESCRIPTION_REGEX = Regex("""^(?<${ItemDescriptionRegexGroups.SOUP}>.*?), ;(?<${ItemDescriptionRegexGroups.REST}>.*)""")

    /**
     * Contains names of regex capture groups inside [ITEM_DESCRIPTION_REGEX].
     */
    private object ItemDescriptionRegexGroups
    {
        const val SOUP = "soup"
        const val REST = "rest"
    }
}
