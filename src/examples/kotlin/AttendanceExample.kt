import kotlinx.coroutines.runBlocking
import me.tomasan7.jecnaapi.JecnaClient
import java.time.LocalDate

fun main(): Unit = runBlocking {

    val client = JecnaClient()

    client.login("user", "password")

    /* Lze specifikovat období v argumentu */
    val attendancesPage = client.getAttendancesPage()

    /* Všechny dny, pro které existují data */
    val attendedDays = attendancesPage.days

    /* Všechny příchody a odchody (v daném půlroce) */
    for (day in attendedDays)
        println(attendancesPage[day])

    /* Můžete vypsat konkrétní příchod a odchod ze dne, který je uložen (prázdný [] pokud den není uložen) */
    println(attendancesPage[LocalDate.of(2023, 3, 30)])
}
