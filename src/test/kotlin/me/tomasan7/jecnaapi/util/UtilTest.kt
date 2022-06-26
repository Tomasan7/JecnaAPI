package me.tomasan7.jecnaapi.util

import java.time.Month
import kotlin.test.Test
import kotlin.test.assertEquals

internal class UtilTest
{
    @Test
    fun testMapToInt()
    {
        val monthRange = Month.FEBRUARY..Month.JUNE
        val monthValueRange = Month.FEBRUARY.value..Month.JUNE.value

        assertEquals(monthValueRange, monthRange.mapToIntRange { it.value })
    }
}