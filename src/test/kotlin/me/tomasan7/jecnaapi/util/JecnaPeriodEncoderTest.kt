package me.tomasan7.jecnaapi.util

import me.tomasan7.jecnaapi.util.JecnaPeriodEncoder.jecnaDecode
import me.tomasan7.jecnaapi.util.JecnaPeriodEncoder.jecnaEncode
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class JecnaPeriodEncoderTest
{
    @Test
    fun testEncodeSchoolYear()
    {
        assertEquals(JecnaPeriodEncoder.SCHOOL_YEAR_ID_KEY to 13, SchoolYear(2021).jecnaEncode())
        assertEquals(JecnaPeriodEncoder.SCHOOL_YEAR_ID_KEY to 11, SchoolYear(2019).jecnaEncode())

        val schoolYear = SchoolYear(2007)
        assertThrows<IllegalArgumentException>("Lowest supported school year is 2008/2009. (got $schoolYear)") {
            JecnaPeriodEncoder.encodeSchoolYear(schoolYear)
        }
    }

    @Test
    fun testDecodeSchoolYear()
    {
        assertEquals(SchoolYear(2021), SchoolYear.jecnaDecode(13))
        assertEquals(SchoolYear(2019), SchoolYear.jecnaDecode(11))

        val id = -1
        assertThrows<IllegalArgumentException>("Id cannot be less than 0. (got $id)") {
            JecnaPeriodEncoder.decodeSchoolYear(id)
        }
    }

    @Test
    fun testEncodeSchoolYearHalf()
    {
        assertEquals(JecnaPeriodEncoder.SCHOOL_YEAR_HALF_ID_KEY to JecnaPeriodEncoder.FIRST_HALF_ID,
                     JecnaPeriodEncoder.encodeSchoolYearHalf(SchoolYearHalf.FIRST))
        assertEquals(JecnaPeriodEncoder.SCHOOL_YEAR_HALF_ID_KEY to JecnaPeriodEncoder.SECOND_HALF_ID,
                     JecnaPeriodEncoder.encodeSchoolYearHalf(SchoolYearHalf.SECOND))
    }

    @Test
    fun testDecodeSchoolYearHalf()
    {
        assertEquals(SchoolYearHalf.FIRST, JecnaPeriodEncoder.decodeSchoolYearHalf(JecnaPeriodEncoder.FIRST_HALF_ID))
        assertEquals(SchoolYearHalf.SECOND, JecnaPeriodEncoder.decodeSchoolYearHalf(JecnaPeriodEncoder.SECOND_HALF_ID))

        val id = 20
        assertThrows<IllegalArgumentException>("Id doesn't correspond to any year half. (got $id)") {
            JecnaPeriodEncoder.decodeSchoolYearHalf(id)
        }
    }

    @Test
    fun testEncodeMonth()
    {
        assertEquals(JecnaPeriodEncoder.MONTH_ID_KEY to 1, JecnaPeriodEncoder.encodeMonth(1))
        assertEquals(JecnaPeriodEncoder.MONTH_ID_KEY to 12, JecnaPeriodEncoder.encodeMonth(12))

        val monthValue = 13
        assertThrows<IllegalArgumentException>("Month must be between 1 and 12. (got $monthValue)") {
            JecnaPeriodEncoder.encodeMonth(13)
        }
    }
}
