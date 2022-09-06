package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.data.article.Article
import me.tomasan7.jecnaapi.data.article.ArticleFile
import me.tomasan7.jecnaapi.data.article.ArticlesPage
import me.tomasan7.jecnaapi.parser.ParseException
import me.tomasan7.jecnaapi.util.emptyMutableLinkedList
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.*

/**
 * Parses correct HTML to [ArticlesPage] instance.
 */
class HtmlArticlesPageParserImpl : HtmlArticlesPageParser
{
    override fun parse(html: String): ArticlesPage
    {
        try
        {
            val articlesPageBuilder = ArticlesPage.builder()

            val document = Jsoup.parse(html)

            val articleEles = document.select(".event")

            for (articleEle in articleEles)
                articlesPageBuilder.addArticle(parseArticle(articleEle))

            return articlesPageBuilder.build()
        }
        catch (e: Exception)
        {
            throw ParseException(e)
        }
    }

    private fun parseArticle(articleEle: Element): Article
    {
        val title = articleEle.selectFirst(".name")!!.text()
        val content = articleEle.selectFirst(".text")!!.text()
        val htmlContent = articleEle.selectFirst(".text")!!.html()
        val articleFileEles = articleEle.select(".files li a")
        val articleFiles = emptyMutableLinkedList<ArticleFile>()

        for (articleFileEle in articleFileEles)
            articleFiles.add(parseArticleFile(articleFileEle))

        val footer = articleEle.selectFirst(".footer")!!.text()
        val (dateStr, author) = footer.split(" | ")
        val date = LocalDate.parse(dateStr, DATE_FORMATTER)

        return Article(title, content, htmlContent, date, author, articleFiles)
    }

    private fun parseArticleFile(articleFileEle: Element): ArticleFile
    {
        val label = articleFileEle.selectFirst(".label")!!.text()
        val downloadPath = articleFileEle.attr("href")

        return ArticleFile(label, downloadPath)
    }

    companion object
    {
        val DATE_FORMATTER: DateTimeFormatter
            get() = DateTimeFormatterBuilder()
                    .appendPattern("d.MMMM")
                    .parseDefaulting(ChronoField.YEAR, LocalDate.now().year.toLong())
                    .toFormatter(Locale.forLanguageTag("cs-CZ"))
    }
}