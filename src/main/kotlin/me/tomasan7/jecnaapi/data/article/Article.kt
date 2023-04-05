@file:UseSerializers(LocalDateSerializer::class)

package me.tomasan7.jecnaapi.data.article

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
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
    val date: LocalDate,
    val author: String,
    val schoolOnly: Boolean,
    val files: List<ArticleFile>,
    /**
     * Images as url paths from the root page.
     */
    val images: List<String>
)
