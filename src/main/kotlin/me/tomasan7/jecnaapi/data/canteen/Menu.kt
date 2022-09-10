package me.tomasan7.jecnaapi.data.canteen

import java.time.LocalDate

data class Menu(private val menu: Map<LocalDate, DayMenu>)
{
    val days = menu.keys
    val daysSorted = menu.keys.sorted()

    fun getDayMenu(day: LocalDate) = menu[day]

    operator fun get(day: LocalDate) = getDayMenu(day)

    companion object
    {
        fun builder() = Builder()
    }

    class Builder
    {
        private val menu = mutableMapOf<LocalDate, DayMenu>()

        fun addDayMenu(day: LocalDate, dayMenu: DayMenu)
        {
            menu[day] = dayMenu
        }

        fun build() = Menu(menu)
    }
}