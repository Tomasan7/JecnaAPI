package me.tomasan7.jecnaapi.util

class SchoolYearRange(
    override val start: SchoolYear,
    override val endInclusive: SchoolYear
) : ClosedRange<SchoolYear>, Iterable<SchoolYear>
{
    override fun iterator() = SchoolYearIterator((start.firstCalendarYear..endInclusive.firstCalendarYear).iterator())
}

class SchoolYearIterator internal constructor(private val intIterator: IntIterator) : Iterator<SchoolYear>
{
    override fun hasNext() = intIterator.hasNext()

    override fun next() = intIterator.next().schoolYear()
}