package me.tomasan7.jecnaapi.util

import java.time.Month
import java.util.LinkedList

/**
 * Creates a new empty mutable [LinkedList].
 */
fun <T> emptyMutableLinkedList() = LinkedList<T>()

/**
 * @return [Month] corresponding to this number.
 * @see [Month.of]
 */
fun Int.month() = Month.of(this)