package me.tomasan7.jecnaapi.repository

import me.tomasan7.jecnaapi.data.canteen.Menu
import me.tomasan7.jecnaapi.data.canteen.MenuItem

/**
 * A client to get and order menus.
 */
interface CanteenClient
{
    suspend fun getMenu(): Menu

    /**
     * Orders the [menuItem].
     *
     * @return Whether the order was successful or not.
     */
    suspend fun order(menuItem: MenuItem): Boolean
}