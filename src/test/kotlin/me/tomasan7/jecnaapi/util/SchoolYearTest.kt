package me.tomasan7.jecnaapi.util

import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnaapi.util.month
import me.tomasan7.jecnaapi.util.schoolYear
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

internal class SchoolYearTest
{
    @Test
    fun testFromString()
    {
        assertEquals(SchoolYear.fromString("2021/2022"), SchoolYear(2021))

        assertThrows<IllegalArgumentException>("SchoolYear String wasn't in correct format. (firstYear/secondYear)") {
            SchoolYear.fromString("2021-2022")
        }

        assertThrows<IllegalArgumentException>("SchoolYear String wasn't in correct format. SecondYear must be equal to firstYear + 1.") {
            SchoolYear.fromString("2021/2023")
        }
    }

    @Test
    fun testPlus()
    {
        assertEquals(SchoolYear(2021), SchoolYear(2019) + 2)
    }

    @Test
    fun testMinus()
    {
        assertEquals(SchoolYear(2021), SchoolYear(2023) - 2)
    }

    @Test
    fun testContains()
    {
        val schoolYear = SchoolYear(2021)

        val date1 = LocalDate.of(2021, 6, 25)
        val date2 = LocalDate.of(2021, 7, 25)
        val date3 = LocalDate.of(2022, 5, 31)
        val date4 = LocalDate.of(2022, 7, 25)

        assertEquals(false, date1 in schoolYear)
        assertEquals(false, date2 in schoolYear)
        assertEquals(true, date3 in schoolYear)
        assertEquals(true, date4 in schoolYear)
    }

    @Test
    fun testGetCalendarYear()
    {
        val schoolYear = SchoolYear(2021)

        assertEquals(2021, schoolYear.getCalendarYear(9.month()))
        assertEquals(2021, schoolYear.getCalendarYear(12.month()))
        assertEquals(2022, schoolYear.getCalendarYear(1.month()))
        assertEquals(2022, schoolYear.getCalendarYear(7.month()))
    }

    @Test
    fun testOfDate()
    {
        assertEquals(SchoolYear(2020), SchoolYear.fromDate(LocalDate.of(2021, 7, 25)))
        assertEquals(SchoolYear(2021), SchoolYear.fromDate(LocalDate.of(2022, 6, 25)))
        assertNotEquals(SchoolYear(2023), SchoolYear.fromDate(LocalDate.of(2022, 7, 25)))
    }

    @Test
    fun testToString() = assertEquals(SchoolYear(2021).toString(), "2021/2022")
}