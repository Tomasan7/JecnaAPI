package me.tomasan7.jecnaapi.parser.parsers

import me.tomasan7.jecnaapi.data.article.Article
import me.tomasan7.jecnaapi.data.article.ArticleFile
import me.tomasan7.jecnaapi.data.article.NewsPage
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
 * Parses correct HTML to [NewsPage] instance.
 */
object HtmlNewsPageParserImpl : HtmlNewsPageParser
{
    override fun parse(html: String): NewsPage
    {
        try
        {
            val newsPageBuilder = NewsPage.builder()

            val document = Jsoup.parse(html)

            val articleEles = document.select(".event")

            for (articleEle in articleEles)
                newsPageBuilder.addArticle(parseArticle(articleEle))

            return newsPageBuilder.build()
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
        val footerSplit = footer.split(" | ")
        val dateStr = footerSplit[0]
        val author = footerSplit[1]
        val schoolOnly = footerSplit.size == 3
        val date = LocalDate.parse(dateStr, DATE_FORMATTER)

        return Article(title, content, htmlContent, date, author, schoolOnly, articleFiles)
    }

    private fun parseArticleFile(articleFileEle: Element): ArticleFile
    {
        val label = articleFileEle.selectFirst(".label")!!.text()
        val downloadPath = articleFileEle.attr("href")

        return ArticleFile(label, downloadPath)
    }

    val DATE_FORMATTER: DateTimeFormatter
        get() = DateTimeFormatterBuilder()
                .appendPattern("d.MMMM")
                .parseDefaulting(ChronoField.YEAR, LocalDate.now().year.toLong())
                .toFormatter(Locale.forLanguageTag("cs-CZ"))
}