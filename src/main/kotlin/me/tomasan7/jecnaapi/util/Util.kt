package me.tomasan7.jecnaapi.util

import java.time.DayOfWeek
import java.time.Month
import java.util.LinkedList

/**
 * Creates a new empty mutable [LinkedList].
 */
fun <T> emptyMutableLinkedList() = LinkedList<T>()

/**
 * @return [Month] corresponding to this number.
 * @throws java.time.DateTimeException if the month-of-year is invalid.
 * @see [Month.of]
 */
fun Int.month(): Month = Month.of(this)

/**
 * Maps any [ClosedRange] to an [IntRange] using [mappingFunction].
 */
fun <T : Comparable<T>> ClosedRange<T>.mapToIntRange(mappingFunction: (T) -> Int): IntRange
{
    val startMapped = mappingFunction(start)
    val endMapped = mappingFunction(endInclusive)

    return startMapped..endMapped
}

/**
 * Sets all [elements] to the [MutableList].
 * Shorthand for
 * ```
 * mutableList.clear()
 * mutableList.addAll(elements)
 * ```
 */
fun <T> MutableCollection<T>.setAll(elements: Iterable<T>)
{
    clear()
    addAll(elements)
}

/**
 * @return The next [day][DayOfWeek] after this one.
 */
fun DayOfWeek.next(): DayOfWeek = DayOfWeek.of(if (this.value == DayOfWeek.values().size) 1 else this.value + 1)