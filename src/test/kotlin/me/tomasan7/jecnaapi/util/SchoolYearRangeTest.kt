package me.tomasan7.jecnaapi.util

import org.junit.jupiter.api.Assertions.*
import kotlin.test.Test

internal class SchoolYearRangeTest
{
    @Test
    fun testSchoolYearRange()
    {
        val actual = (2020.schoolYear()..2024.schoolYear()).toList()
        val expected = listOf(
            2020.schoolYear(),
            2021.schoolYear(),
            2022.schoolYear(),
            2023.schoolYear(),
            2024.schoolYear(),
        )

        assertEquals(expected, actual)

        assertEquals(emptyList<SchoolYear>(), (2022.schoolYear()..2020.schoolYear()).toList())
    }
}