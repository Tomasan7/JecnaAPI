package me.tomasan7.jecnaapi.data.timetable

import kotlinx.serialization.Serializable
import me.tomasan7.jecnaapi.serialization.TimetableSerializer
import me.tomasan7.jecnaapi.util.emptyMutableLinkedList
import me.tomasan7.jecnaapi.util.next
import me.tomasan7.jecnaapi.util.setAll
import java.time.*
import java.time.temporal.ChronoUnit
import java.util.*

@Serializable(with = TimetableSerializer::class)
class Timetable private constructor(
    lessonPeriods: List<LessonPeriod>,
    private val timetable: Map<DayOfWeek, List<LessonSpot>>
)
{
    val lessonPeriods = lessonPeriods.sortedBy { it.from }

    /** All days in the [Timetable]. There may be no [LessonSpots][LessonSpot] for some days. */
    val days = this.timetable.keys

    /**
     * All days in the [Timetable] sorted as they would be in a week.
     * There may be no [LessonSpots][LessonSpot] for some days.
     */
    val daysSorted = days.sorted()

    /**
     * Returns Whether the [Timetable] contains any days.
     * There may be no [LessonSpots][LessonSpot] for some days, that would return `false`.
     */
    fun isEmpty() = timetable.isEmpty()

    /** Returns index of the [LessonPeriod] at the given [time], or `null` if there is not any. */
    fun getIndexOfLessonPeriod(time: LocalTime) = lessonPeriods.indexOfFirst { time in it }.let { if (it != -1) it else null }

    /** Returns index of the [LessonPeriod] at [LocalTime.now], or `null` if there is not any. */
    fun getIndexOfCurrentLessonPeriod() = getIndexOfLessonPeriod(LocalTime.now())

    /** Returns the [LessonPeriod] at the given [time], or `null` if there is not any. */
    fun getLessonPeriod(time: LocalTime) = getIndexOfLessonPeriod(time)?.let { lessonPeriods[it] }

    /** Returns the [LessonPeriod] at [LocalTime.now], or `null` if there is not any. */
    fun getCurrentLessonPeriod() = getLessonPeriod(LocalTime.now())

    /** Returns the index of next [LessonPeriod] from the given [time], or `null` if there is no next [LessonPeriod]. */
    fun getIndexOfNextLessonPeriod(time: LocalTime): Int?
    {
        fun minsUntilStartOf(lessonPeriodIndex: Int) = time.until(lessonPeriods[lessonPeriodIndex].from, ChronoUnit.MINUTES)

        return lessonPeriods.indices.filter { minsUntilStartOf(it) > 0 }.minByOrNull { minsUntilStartOf(it) }
    }

    /** Returns the index of next [LessonPeriod] from [LocalTime.now], or `null` if there is no next [LessonPeriod]. */
    fun getIndexOfCurrentNextLessonPeriod() = getIndexOfNextLessonPeriod(LocalTime.now())

    /** Returns the next [LessonPeriod] from the given [time], or `null` if there is no next [LessonPeriod]. */
    fun getNextLessonPeriod(time: LocalTime) = getIndexOfNextLessonPeriod(time)?.let { lessonPeriods[it] }

    /** Returns the next [LessonPeriod] from [LocalTime.now], or `null` if there is no next [LessonPeriod]. */
    fun getCurrentNextLessonPeriod() = getNextLessonPeriod(LocalTime.now())

    /** Returns a list of all [LessonSpots][LessonSpot] in the given [day] ordered by their start time, or `null` if [day] is not in this [Timetable]. */
    fun getLessonSpotsForDay(day: DayOfWeek) = timetable[day]

    /** Returns a list of all [LessonSpots][LessonSpot] in the given [day] ordered by their start time, or `null` if [day] is not in this [Timetable]. */
    operator fun get(day: DayOfWeek) = getLessonSpotsForDay(day)

    /** Returns the [LessonSpot] that is happening at the given [lessonPeriodIndex]. */
    fun getLessonSpot(day: DayOfWeek, lessonPeriodIndex: Int): LessonSpot?
    {
        if (isEmpty()) return null

        val lessonSpots = getLessonSpotsForDay(day) ?: return null

        var x = 0

        for (lessonSpot in lessonSpots)
        {
            if (lessonPeriodIndex in x until x + lessonSpot.periodSpan)
                return lessonSpot

            x += lessonSpot.periodSpan
        }

        return null
    }

    /**
     * Returns the [LessonSpot] that is happening at the given [lessonPeriod].
     * Prefer using [getLessonSpot] if you know the [lessonPeriod] index.
     * @see getLessonSpot
     */
    fun getLessonSpot(day: DayOfWeek, lessonPeriod: LessonPeriod) = lessonPeriods.indexOf(lessonPeriod).let { if (it != -1) getLessonSpot(day, it) else null }

    /**
     * Returns the [LessonSpot] at the given [day] and [time], or `null` if there is no [LessonSpot] at that moment.
     *
     * @param takeEmpty Whether [empty][LessonSpot.isEmpty] [LessonSpot] should be returned, or `null` instead.
     */
    fun getLessonSpot(day: DayOfWeek, time: LocalTime, takeEmpty: Boolean = false): LessonSpot?
    {
        val lessonPeriodIndex = getIndexOfLessonPeriod(time) ?: return null

        return getLessonSpot(day, lessonPeriodIndex)?.takeIf { takeEmpty || it.isNotEmpty() }
    }

    /**
     * Returns the [LessonSpot] at the given [datetime], or `null` if there is no [LessonSpot] at that moment.
     *
     * @param takeEmpty Whether [empty][LessonSpot.isEmpty] [LessonSpot] should be returned, or `null` instead.
     */
    fun getLessonSpot(datetime: LocalDateTime, takeEmpty: Boolean = false) = getLessonSpot(datetime.dayOfWeek, datetime.toLocalTime(), takeEmpty)

    /**
     * Returns the [LessonSpot] at the given [instant], or `null` if there is no [LessonSpot] at that moment.
     *
     * @param takeEmpty Whether [empty][LessonSpot.isEmpty] [LessonSpot] should be returned, or `null` instead.
     */
    fun getLessonSpot(instant: Instant, takeEmpty: Boolean = false) = getLessonSpot(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()), takeEmpty)

    /**
     * Returns the [LessonSpot] at [Instant.now], or `null` if there is currently no [LessonSpot].
     *
     * @param takeEmpty Whether [empty][LessonSpot.isEmpty] [LessonSpot] should be returned, or `null` instead.
     */
    fun getCurrentLessonSpot(takeEmpty: Boolean = false) = getLessonSpot(Instant.now(), takeEmpty)

    /**
     * Returns the next [LessonSpot] from the given [time] on the [day], or `null` if there is no [LessonSpot] at that moment.
     *
     * @param takeEmpty Whether [empty][LessonSpot.isEmpty] [LessonSpot] should be returned, or `null` instead.
     */
    fun getNextLessonSpot(day: DayOfWeek, time: LocalTime, takeEmpty: Boolean = false): LessonSpot?
    {
        if (timetable.isEmpty()) return null

        if (day !in days)
            return getNextLessonSpot(day.next(), LocalTime.of(0, 0), takeEmpty)

        val nextLessonPeriodIndex = getIndexOfLessonPeriod(time) ?: 0

        val nextLessonSpot = getLessonSpot(day, nextLessonPeriodIndex)?.takeIf { takeEmpty || it.isNotEmpty() }

        return nextLessonSpot ?: getNextLessonSpot(day.next(), LocalTime.of(0, 0), takeEmpty)
    }

    /**
     * Returns the next [LessonSpot] from the given [datetime], or `null` if there is no [LessonSpot] at that moment.
     *
     * @param takeEmpty Whether [empty][LessonSpot.isEmpty] [LessonSpot] should be returned, or `null` instead.
     */
    fun getNextLessonSpot(datetime: LocalDateTime, takeEmpty: Boolean = false) = getNextLessonSpot(datetime.dayOfWeek, datetime.toLocalTime(), takeEmpty)

    /**
     * Returns the next [LessonSpot] from the given [instant], or `null` if there is no [LessonSpot] at that moment.
     *
     * @param takeEmpty Whether [empty][LessonSpot.isEmpty] [LessonSpot] should be returned, or `null` instead.
     */
    fun getNextLessonSpot(instant: Instant, takeEmpty: Boolean = false) = getNextLessonSpot(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()), takeEmpty)

    /**
     * Returns the next [LessonSpot] from [Instant.now], or `null` if there is no [LessonSpot] at that moment.
     *
     * @param takeEmpty Whether [empty][LessonSpot.isEmpty] [LessonSpot] should be returned, or `null` instead.
     */
    fun getCurrentNextLessonSpot(takeEmpty: Boolean = false) = getNextLessonSpot(Instant.now(), takeEmpty)

    companion object
    {
        fun builder() = Builder()
    }

    class Builder
    {
        private val lessonPeriods: MutableList<LessonPeriod> = emptyMutableLinkedList()
        private val timetable: MutableMap<DayOfWeek, MutableList<LessonSpot>> = TreeMap()

        /**
         * Sets all the [LessonPeriods][LessonPeriod].
         *
         * @param lessonPeriods The [LessonPeriods][LessonPeriod] to use.
         * @return This [builder's][Builder] instance back.
         */
        fun setLessonPeriods(lessonPeriods: Iterable<LessonPeriod>): Builder
        {
            this.lessonPeriods.setAll(lessonPeriods)
            return this
        }

        /**
         * Sets a [LessonPeriod] to a specified hour index.
         *
         * @param hour       The hour index to set the [LessonPeriod] to.
         * @param lessonPeriod The [LessonPeriod] to set.
         * @return This [builder's][Builder] instance back.
         */
        fun setLessonPeriod(hour: Int, lessonPeriod: LessonPeriod): Builder
        {
            lessonPeriods[hour] = lessonPeriod
            return this
        }

        /**
         * Adds a [LessonPeriod] to the [TimetablePage].
         *
         * @param lessonPeriod The [LessonPeriod] to add.
         * @return This [builder's][Builder] instance back.
         */
        fun addLessonPeriod(lessonPeriod: LessonPeriod): Builder
        {
            lessonPeriods.add(lessonPeriod)
            return this
        }

        /**
         * Sets all the [LessonSpots][LessonSpot].
         *
         * @param day The day to set the [lessonSpots] to.
         * @param lessonSpots The [LessonSpots][LessonSpot] to use.
         * @return This [builder's][Builder] instance back.
         */
        fun setLessonSpots(day: DayOfWeek, lessonSpots: Iterable<LessonSpot>): Builder
        {
            this.timetable.computeIfAbsent(day) { emptyMutableLinkedList() }.setAll(lessonSpots)
            return this
        }

        /**
         * Adds a [LessonSpot] to a day.
         * Can be `null`, if there is no lesson at that time.
         * The [LessonSpot] gets appended to the end.
         *
         * @param day    The day to add the [LessonSpot] to.
         * @param lessonSpot The [LessonSpot] to add.
         * @return This [builder's][Builder] instance back.
         */
        fun addLessonSpot(day: DayOfWeek, lessonSpot: LessonSpot): Builder
        {
            /* Gets the list for the day, if none is present, creates a new list and puts it into the map. Then the lesson is added to that list. */
            timetable.computeIfAbsent(day) { emptyMutableLinkedList() }.add(lessonSpot)
            return this
        }

        /**
         * Shorthand for `addLessonSpot(day, hour, new LessonSpot(lesson))`.
         * @see .setLessonSpot
         * @see LessonSpot
         */
        fun addLesson(day: DayOfWeek, lesson: Lesson): Builder
        {
            /* Gets the list for the day, if none is present, creates a new list and puts it into the map. Then the lesson is added to that list. */
            timetable.computeIfAbsent(day) { emptyMutableLinkedList() }.add(LessonSpot(lesson))
            return this
        }

        fun build(): Timetable
        {
            /* TODO: Maybe check if there is equal or more lessonPeriods than lessons in any day?
             * Because that would mean there is a lesson without specified LessonPeriod. */
            return Timetable(lessonPeriods, timetable)
        }
    }
}