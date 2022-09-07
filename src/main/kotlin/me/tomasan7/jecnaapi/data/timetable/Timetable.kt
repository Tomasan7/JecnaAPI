package me.tomasan7.jecnaapi.data.timetable

import me.tomasan7.jecnaapi.util.emptyMutableLinkedList
import me.tomasan7.jecnaapi.util.next
import me.tomasan7.jecnaapi.util.setAll
import java.time.*
import java.time.temporal.ChronoUnit
import java.util.*

class Timetable(lessonPeriods: List<LessonPeriod>, timetable: Map<DayOfWeek, List<LessonSpot>>)
{
    val lessonPeriods: List<LessonPeriod>
    val timetable: Map<DayOfWeek, List<TimetableSpot>>

    init
    {
        this.lessonPeriods = lessonPeriods.sortedBy { it.from }
        this.timetable = timetable.mapValues { entry ->
            entry.value.mapIndexed { i, lessonSpot ->
                TimetableSpot(entry.key, this.lessonPeriods[i], lessonSpot)
            }
        }
    }

    /**
     * All [days][DayOfWeek] in the [Timetable]. There may be no [TimetableSpots][TimetableSpot] for some days.
     */
    val days = this.timetable.keys

    /**
     * All [days][DayOfWeek] in the [Timetable] sorted as they would be in a week. There may be no [TimetableSpots][TimetableSpot] for some days.
     */
    val daysSorted = days.sorted()

    /**
     * @return Whether the [Timetable] contains any [days][DayOfWeek].
     * There may be no [TimetableSpots][TimetableSpot] for some days, that would return `false`.
     */
    fun isEmpty() = timetable.isEmpty()

    /**
     * @return A [List] of all [TimetableSpots][TimetableSpot] in the [day] ordered by their start time.
     */
    fun getTimetableSpotsForDay(day: DayOfWeek) = timetable[day]

    /**
     * @return A [List] of all [TimetableSpots][TimetableSpot] in the [day] ordered by their start time.
     */
    operator fun get(day: DayOfWeek) = timetable[day]

    /**
     * @param takeEmpty Whether [empty][TimetableSpot.isEmpty] [TimetableSpot] should be returned.
     * @return The [TimetableSpot] at the provided [day] and [time]. `null` if there is currently no [LessonSpot].
     */
    fun getTimetableSpot(day: DayOfWeek, time: LocalTime, takeEmpty: Boolean = false): TimetableSpot?
    {
        return timetable[day]?.find { time in it.lessonPeriod }?.takeIf { takeEmpty || !it.lessonSpot.isEmpty() }
    }

    /**
     * @param takeEmpty Whether [empty][TimetableSpot.isEmpty] [TimetableSpot] should be returned.
     * @return The [TimetableSpot] at the provided [datetime]. `null` if there is currently no [LessonSpot].
     */
    fun getTimetableSpot(datetime: LocalDateTime, takeEmpty: Boolean = false) = getTimetableSpot(datetime.dayOfWeek, datetime.toLocalTime(), takeEmpty)

    /**
     * @param takeEmpty Whether [empty][TimetableSpot.isEmpty] [TimetableSpot] should be returned.
     * @return The [TimetableSpot] at the provided [instant]. `null` if there is currently no [LessonSpot].
     */
    fun getTimetableSpot(instant: Instant, takeEmpty: Boolean = false) = getTimetableSpot(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()), takeEmpty)

    /**
     * @param takeEmpty Whether [empty][TimetableSpot.isEmpty] [TimetableSpot] should be returned.
     * @return The current [TimetableSpot] at the moment. `null` if there is currently no [LessonSpot].
     */
    fun getCurrentTimetableSpot(takeEmpty: Boolean = false) = getTimetableSpot(Instant.now(), takeEmpty)

    /**
     * @param takeEmpty Whether [empty][TimetableSpot.isEmpty] [TimetableSpot] should be returned.
     * @return The following [TimetableSpot] from the provided [day] and [time]. Or `null`, when the [Timetable] is [empty][isEmpty].
    */
    fun getNextTimetableSpot(day: DayOfWeek, time: LocalTime, takeEmpty: Boolean = false): TimetableSpot?
    {
        if (timetable.isEmpty())
            return null

        if (!timetable.containsKey(day))
            return getNextTimetableSpot(day.next(), LocalTime.of(0, 0), takeEmpty)

        val daySpots = timetable[day]!!

        fun TimetableSpot.isViable() = takeEmpty || !isEmpty()

        var lastDiff: Pair<TimetableSpot, Long>? = null

        for (timetableSpot in daySpots)
        {
            if (timetableSpot.lessonPeriod.from < time)
                continue

            val diff = time.until(timetableSpot.lessonPeriod.from, ChronoUnit.SECONDS)

            if (lastDiff == null && timetableSpot.isViable())
            {
                lastDiff = timetableSpot to diff
                continue
            }

            if (lastDiff != null && diff > lastDiff.second)
                break

            if (timetableSpot.isViable())
                lastDiff = timetableSpot to diff
        }

        if (lastDiff == null)
            return getNextTimetableSpot(day.next(), LocalTime.of(0, 0), takeEmpty)

        return lastDiff.first
    }

    /**
     * @param takeEmpty Whether [empty][TimetableSpot.isEmpty] [TimetableSpot] should be returned.
     * @return The following [TimetableSpot] from the provided [datetime]. Or `null`, when the [Timetable] is [empty][isEmpty].
     */
    fun getNextTimetableSpot(datetime: LocalDateTime, takeEmpty: Boolean = false) = getNextTimetableSpot(datetime.dayOfWeek, datetime.toLocalTime(), takeEmpty)

    /**
     * @param takeEmpty Whether [empty][TimetableSpot.isEmpty] [TimetableSpot] should be returned.
     * @return The following [TimetableSpot] from the provided [instant]. Or `null`, when the [Timetable] is [empty][isEmpty].
     */
    fun getNextTimetableSpot(instant: Instant, takeEmpty: Boolean = false) = getNextTimetableSpot(LocalDateTime.ofInstant(instant, ZoneId.systemDefault()), takeEmpty)

    /**
     * @param takeEmpty Whether [empty][TimetableSpot.isEmpty] [TimetableSpot] should be returned.
     * @return The currently following [TimetableSpot]. Or `null`, when the [Timetable] is [empty][isEmpty].
     */
    fun getCurrentNextTimetableSpot(takeEmpty: Boolean = false) = getNextTimetableSpot(Instant.now(), takeEmpty)

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