package me.tomasan7.jecnaapi.repository

import me.tomasan7.jecnaapi.data.canteen.DayMenu
import me.tomasan7.jecnaapi.data.canteen.Menu
import me.tomasan7.jecnaapi.data.canteen.MenuItem
import java.time.LocalDate

/**
 * A client to get and order menus.
 */
interface CanteenClient
{
    suspend fun getMenu(): Menu

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
     * @param menu The [Menu] the [menuItem] is in.
     * @return Whether the order was successful or not.
     */
    suspend fun order(menuItem: MenuItem, dayMenuDay: LocalDate, menu: Menu): Boolean

    /**
     * Orders the [menuItem].
     * And updates whole [Menu] accordingly.
     * Use [order] with [dayMenuDay][LocalDate] parameter, if you have it.
     * This function finds the [day][LocalDate] of the [menuItem] in the [menu].
     *
     * @param menuItem The [MenuItem] to order.
     * @param dayMenu The [DayMenu] the [menuItem] is in.
     * @param menu The [Menu] the [menuItem] is in.
     * @return Whether the order was successful or not.
     */
    suspend fun order(menuItem: MenuItem, dayMenu: DayMenu, menu: Menu) = order(menuItem, dayMenu.day, menu)

    /**
     * Orders the [menuItem].
     * And updates whole [Menu] accordingly.
     * Use [order] with [dayMenuDay][LocalDate] parameter, if you have it.
     * This function finds the [day][LocalDate] of the [menuItem] in the [menu].
     *
     * @param menuItem The [MenuItem] to order.
     * @param menu The [Menu] the [menuItem] is in.
     * @return Whether the order was successful or not.
     */
    suspend fun order(menuItem: MenuItem, menu: Menu) = order(menuItem, menu.dayMenus.find { it.items.contains(menuItem) }!!.day, menu)
}