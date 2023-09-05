package me.tomasan7.jecnaapi.web.canteen

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import me.tomasan7.jecnaapi.parser.parsers.selectFirstOrThrow
import me.tomasan7.jecnaapi.web.Auth
import me.tomasan7.jecnaapi.web.AuthWebClient
import org.jsoup.Jsoup

class ICanteenWebClient : AuthWebClient
{
    private val cookieStorage = AcceptAllCookiesStorage()

    private val httpClient = HttpClient(CIO) {
        install(HttpCookies) {
            storage = cookieStorage
        }

        followRedirects = false
    }

    override suspend fun login(auth: Auth): Boolean
    {
        val loginFormHtmlResponse = queryStringBody("/login")
        val loginPostResponse = httpClient.submitForm(
            block = newRequestBuilder("/j_spring_security_check"),
            formParameters = Parameters.build {
                append("j_username", auth.username)
                append("j_password", auth.password)
                append("terminal", false.toString())
                append("type", "web")
                append("_csrf", Jsoup.parse(loginFormHtmlResponse)
                    .selectFirstOrThrow("#signup-user-col > div > form > ul > li > input[name=_csrf]", "CSRF token")
                    .attr("value"))
                append("targetUrl", "/faces/secured/main.jsp?terminal=false&status=true&printer=&keyboard=")
            })

        /* If the login was unsuccessful, the web redirects back to the login page. */
        return !loginPostResponse.headers[HttpHeaders.Location]!!.startsWith("/login")
    }

    override suspend fun logout()
    {
        val logoutFormResponse = query("/faces/secured/mobile.jsp")

        val locationHeader = logoutFormResponse.headers[HttpHeaders.Location]

        /* Is true when no one is logged in. */
        if (locationHeader != null && locationHeader.endsWith("/login"))
            return

        httpClient.submitForm(
            block = newRequestBuilder("/logout"),
            formParameters = Parameters.build {
                append(
                    name = "_csrf",
                    value = Jsoup.parse(logoutFormResponse.bodyAsText())
                        .selectFirstOrThrow("#logout > input[name=_csrf]", "CSRF token").attr("value")
                )
            })
    }

    override suspend fun isLoggedIn() = !query("/faces/secured/main.jsp").headers.contains("Location")

    override suspend fun query(path: String, parameters: Parameters?): HttpResponse
    {
        val response = httpClient.get(newRequestBuilder(path, parameters))
        return response
    }

    /**
     * Returns a function modifying [HttpRequestBuilder] used by Ktor HttpClient.
     * Sets the url relative to [ENDPOINT].
     *
     * @param path The path to query. Must include first slash.
     * @param parameters HTTP parameters, which will be sent URL encoded.
     * @param block Additional modifications to the request.
     * @return The function.
     */
    private fun newRequestBuilder(
        path: String,
        parameters: Parameters? = null,
        block: (HttpRequestBuilder.() -> Unit)? = null
    ): HttpRequestBuilder.() -> Unit
    {
        return {
            if (block != null)
                block()

            url(urlString = ENDPOINT + path)

            if (parameters != null)
                url { this.parameters.appendAll(parameters) }
        }
    }

    companion object
    {
        const val ENDPOINT = "https://strav.nasejidelna.cz/0341"
    }
}
