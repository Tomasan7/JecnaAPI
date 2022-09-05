package me.tomasan7.jecnaapi.data.article

/**
 * An attachment file to the [articles][Article] on the main news page.
 *
 * @property downloadPath A relative path to the file from the page's root. ("/")
 */
data class ArticleFile(
    val label: String,
    val downloadPath: String,
    val filename: String = downloadPath.split("/").last()
)
{
    val fileNameNoExtension = filename.split(FILE_EXTENSION_DOT_REGEX)[0]
    val fileExtension = filename.split(FILE_EXTENSION_DOT_REGEX)[1]

    companion object
    {
        /**
         * Matches the dot between file name and extension.
         */
        val FILE_EXTENSION_DOT_REGEX = Regex("""\.(?!.*\.)""")
    }
}