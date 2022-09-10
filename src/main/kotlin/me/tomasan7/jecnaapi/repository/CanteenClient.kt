package me.tomasan7.jecnaapi.repository

import me.tomasan7.jecnaapi.data.canteen.Menu
import me.tomasan7.jecnaapi.data.canteen.MenuItem

interface CanteenClient
{
    suspend fun getMenu(): Menu

    suspend fun order(menuItem: MenuItem): Boolean
}