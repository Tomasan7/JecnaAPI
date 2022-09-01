package me.tomasan7.jecnaapi.data

/**
 * An attachment file to the [articles][Article] on the main news page.
 *
 * @property downloadPath A relative path to the file from the page's root. ("/")
 */
data class ArticleFile(
    val label: String,
    val downloadPath: String
)