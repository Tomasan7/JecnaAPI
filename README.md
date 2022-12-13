# JecnaAPI

JecnaAPI je Kotlin knihovna, díky které lze přistupovat k datům webu [spsejecna.cz](https://spsejecna.cz).

## Funkce

- čtení:
  - Novinky
  - Známky
  - Rozvrh
  - Příchody a odchody
  - Učitelský sbor
  - Obědy

- obědnávání obědů
- dávání obědů do/z burzy
- odkupování obědů z burzy

## Použití

### Vytvoření JecnaClient objektu

```kotlin
val jecnaClient = JecnaClient("username", "password")
```

### Přihlášení

Přihlášení je nezbytné k čtení dat studenta.

```kotlin
/* runBlocking, nebo jiný coroutine scope. */
runBlocking {
    jecnaClient.login()
}
```

### Čtení dat

```kotlin
/* runBlocking, nebo jiný coroutine scope. */
runBlocking {
  val newsPage = jecnaClient.getNewsPage()
  val gradesPage = jecnaClient.getGradesPage()
  val timetablePage = jecnaClient.getTimetablePage()
  val attendancePage = jecnaClient.getAttendancePage()
  val teachersPage = jecnaClient.getTeachersPage()
}
```

Některé metody berou obdoví (např. rok) jako parametr.

```kotlin
/* runBlocking, nebo jiný coroutine scope. */
runBlocking {
/* Získání známek z roku 2021/2022 z druhého pololetí.  */
  val gradesPage = jecnaClient.getGradesPage(SchoolYear(2021), SchoolYearHalf.SECOND)
}
```

Více příkladů najdete ve složce [examples](/src/main/kotlin/examples).