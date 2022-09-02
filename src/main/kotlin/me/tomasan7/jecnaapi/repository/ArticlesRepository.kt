package me.tomasan7.jecnaapi.repository

import me.tomasan7.jecnaapi.data.ArticlesPage

/**
 * Retrieves [ArticlesPage] from any kind of data source.
 */
interface ArticlesRepository
{
    suspend fun queryArticlesPage(): ArticlesPage
}