package me.tomasan7.jecnaapi.data.article

import java.time.LocalDate

/**
 * An article on the main news page.
 */
data class Article(
    val title: String,
    val content: String,
    val htmlContent: String,
    val date: LocalDate,
    val author: String,
    val files: List<ArticleFile>
)