package me.tomasan7.jecnaapi.repository

import me.tomasan7.jecnaapi.data.canteen.DayMenu
import me.tomasan7.jecnaapi.data.canteen.MenuItem
import me.tomasan7.jecnaapi.data.canteen.Menu
import me.tomasan7.jecnaapi.data.canteen.MenuPage
import java.time.LocalDate

/**
 * A client to get and order menus.
 */
interface CanteenClient
{
    suspend fun getMenuPage(): MenuPage

    /**
     * Orders the [menuItem].
     *
     * @param menuItem The [MenuItem] to order.
     * @return Whether the order was successful or not.
     */
    suspend fun order(menuItem: MenuItem): Boolean

    /**
     * Orders the [menuItem].
     * And updates whole [Menu] accordingly.
     *
     * @param menuItem The [MenuItem] to order.
     * @param dayMenuDay the [day][LocalDate] of the [DayMenu] the [menuItem] is in.
     * @param menuPage The [Menu] the [menuItem] is in.
     * @return Whether the order was successful or not.
     */
    suspend fun order(menuItem: MenuItem, dayMenuDay: LocalDate, menuPage: MenuPage): Boolean

    /**
     * Orders the [menuItem].
     * And updates whole [Menu] accordingly.
     * Use [order] with [dayMenuDay][LocalDate] parameter, if you have it.
     * This function finds the [day][LocalDate] of the [menuItem] in the [menuPage].
     *
     * @param menuItem The [MenuItem] to order.
     * @param dayMenu The [DayMenu] the [menuItem] is in.
     * @param menuPage The [MenuPage] the [menuItem] is in.
     * @return Whether the order was successful or not.
     */
    suspend fun order(menuItem: MenuItem, dayMenu: DayMenu, menuPage: MenuPage) = order(menuItem, dayMenu.day, menuPage)

    /**
     * Orders the [menuItem].
     * And updates whole [Menu] accordingly.
     * Use [order] with [dayMenuDay][LocalDate] parameter, if you have it.
     * This function finds the [day][LocalDate] of the [menuItem] in the [menuPage].
     *
     * @param menuItem The [MenuItem] to order.
     * @param menuPage The [MenuPage] the [menuItem] is in.
     * @return Whether the order was successful or not.
     */
    suspend fun order(menuItem: MenuItem, menuPage: MenuPage) = order(menuItem, menuPage.menu.dayMenus.find { it.items.contains(menuItem) }!!.day, menuPage)
}