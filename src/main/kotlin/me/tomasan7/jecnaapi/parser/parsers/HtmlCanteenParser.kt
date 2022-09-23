package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.data.canteen.DayMenu
import me.tomasan7.jecnaapi.data.canteen.Menu
import me.tomasan7.jecnaapi.data.canteen.MenuPage

/**
 * Is responsible for parsing HTML source code in [String] to [Menu] instance.
 */
interface HtmlCanteenParser
{
    /**
     * @throws me.tomasan7.jecnaapi.parser.ParseException When the HTML source isn't in correct format.
     */
    fun parse(html: String): MenuPage

    fun parseDayMenu(html: String): DayMenu
}