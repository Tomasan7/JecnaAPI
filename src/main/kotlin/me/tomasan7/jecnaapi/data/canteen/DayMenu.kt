package me.tomasan7.jecnaapi.data.canteen

import me.tomasan7.jecnaapi.util.emptyMutableLinkedList
import java.time.LocalDate

/**
 * A menu for a [day][LocalDate].
 */
data class DayMenu(
    val day: LocalDate,
    val items: List<MenuItem>
) : Iterable<MenuItem>
{
    override fun iterator() = items.iterator()

    companion object
    {
        fun builder(day: LocalDate) = Builder(day)
    }

    class Builder(private val day: LocalDate)
    {
        private val items = emptyMutableLinkedList<MenuItem>()

        fun addMenuItem(menuItem: MenuItem) = items.add(menuItem)

        fun build() = DayMenu(day, items)
    }
}
