import kotlinx.coroutines.runBlocking
import me.tomasan7.jecnaapi.JecnaClient
import java.time.DayOfWeek
import java.time.LocalTime

fun main(): Unit = runBlocking {

    val client = JecnaClient()

    client.login("user", "password")

    val timetablePage = client.getTimetablePage()

    /* Celý rozvrh */
    for (days in timetablePage.timetable.days)
        println(timetablePage.timetable[days])

    /* Od kdy do kdy trvá hodina v daném čase (null pokud není hodina/ je přestávka) */
    val lessonTimeSpan = timetablePage.timetable.getLessonPeriod(LocalTime.of(8, 35))
    println(lessonTimeSpan)

    /* Od kdy do kdy trvá hodina v čase nynějším (null pokud není hodina/ je přestávka) */
    val currentTimeLessonTimeSpan = timetablePage.timetable.getCurrentLessonPeriod()
    println(println(currentTimeLessonTimeSpan))

    /* Vrátí časové rozmezí následující po nynější hodině */
    val upcomingLesson = timetablePage.timetable.getNextLessonPeriod(LocalTime.of(8, 35))
    println(upcomingLesson)

    /* Vrátí všechny hodiny v daném dni */
    val lessonsInCertainDay = timetablePage.timetable[DayOfWeek.WEDNESDAY]
    println(lessonsInCertainDay)

    println()
    /* Vrátí konkrétní hodinu daného dne, v určeném čase */
    val lessonDuringDayAndTime = timetablePage.timetable.getLessonSpot(DayOfWeek.TUESDAY, LocalTime.of(10,45))
    println(lessonDuringDayAndTime)
}
