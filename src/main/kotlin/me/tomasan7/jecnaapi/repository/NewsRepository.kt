package me.tomasan7.jecnaapi.repository

import me.tomasan7.jecnaapi.data.article.NewsPage

/**
 * Retrieves [NewsPage] from any kind of data source.
 */
interface NewsRepository
{
    suspend fun queryArticlesPage(): NewsPage
}