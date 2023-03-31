package examples

import kotlinx.coroutines.runBlocking
import me.tomasan7.jecnaapi.JecnaClient
import me.tomasan7.jecnaapi.util.SchoolYear
import me.tomasan7.jecnaapi.util.SchoolYearHalf

fun main(): Unit = runBlocking {

    val client = JecnaClient()

    client.login("user", "password")

    /* Stáhne známky z roku 2021/2022 z druhého pololetí. */
    val gradesPage = client.getGradesPage(SchoolYear(2021), SchoolYearHalf.SECOND)

    val mathSubject = gradesPage["Matematika"]!!

    val mathAverage = mathSubject.grades.average()

    println("Průměr z matematiky: $mathAverage")

    /* Do grades[] patří string, který určuje část předmětu. (např. Teorie, Cvičení)
    * Pokud předmět není rozdělen, použijte null. */
    val mathGrades = mathSubject.grades[null]!!

    println("Známky z matematiky: ")

    for (grade in mathGrades)
        println(grade.valueChar() + " - " + grade.description)
}
