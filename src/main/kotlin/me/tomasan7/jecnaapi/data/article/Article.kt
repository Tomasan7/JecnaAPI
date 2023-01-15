package me.tomasan7.jecnaapi.data.article

import kotlinx.serialization.Serializable
import me.tomasan7.jecnaapi.serialization.LocalDateSerializer
import java.time.LocalDate

/**
 * An article on the main news page.
 */
@Serializable
data class Article(
    val title: String,
    val content: String,
    val htmlContent: String,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    val author: String,
    val schoolOnly: Boolean,
    val files: List<ArticleFile>,
    /**
     * Images as url paths from the root page.
     */
    val images: List<String>
)