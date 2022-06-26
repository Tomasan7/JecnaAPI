package me.tomasan7.jecnaapi.data

import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnaapi.util.month
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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
    fun testContains()
    {
        val schoolYear = SchoolYear(2021)

        val date1 = LocalDate.of(2022, 5, 31)
        val date2 = LocalDate.of(2021, 7, 25)
        val date3 = LocalDate.of(2022, 7, 25)

        assertEquals(true, date1 in schoolYear)
        assertEquals(true, date2 in schoolYear)
        assertEquals(false, date3 in schoolYear)
    }

    @Test
    fun testGetCalendarYear()
    {
        val schoolYear = SchoolYear(2021)

        assertEquals(2022, schoolYear.getCalendarYear(6.month()))
        assertEquals(2022, schoolYear.getCalendarYear(1.month()))
        assertEquals(2021, schoolYear.getCalendarYear(7.month()))
        assertEquals(2021, schoolYear.getCalendarYear(12.month()))
    }

    @Test
    fun testOfDate()
    {
        assertEquals(SchoolYear(2021), SchoolYear(LocalDate.of(2021, 7, 25)))
        assertNotEquals(SchoolYear(2023), SchoolYear(LocalDate.of(2022, 7, 25)))
    }

    @Test
    fun testToString() = assertEquals(SchoolYear(2021).toString(), "2021/2022")
}