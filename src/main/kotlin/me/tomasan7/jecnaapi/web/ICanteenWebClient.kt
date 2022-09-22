package me.tomasan7.jecnaapi.web

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.http.*
import me.tomasan7.jecnaapi.web.JecnaWebClient.Companion.ENDPOINT
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
        val loginFormHtmlResponse = queryStringBody("/faces/login.jsp")
        val loginPostResponse = httpClient.post(newRequestBuilder("/j_spring_security_check", Parameters.build {
            append("j_username", auth.username)
            append("j_password", auth.password)
            append("terminal", false.toString())
            append("type", "web")
            append("_csrf", Jsoup.parse(loginFormHtmlResponse)
                    .select("#signup-user-col > div > form > ul > li > input[name=_csrf]").attr("value"))
            append("targetUrl", "/faces/secured/main.jsp?terminal=false&status=true&printer=&keyboard=")
        }))

        /* If the login was unsuccessful, the web redirects back to the login page. */
        return !loginPostResponse.headers[HttpHeaders.Location]!!.startsWith("/faces/login.jsp")
    }

    override suspend fun isLoggedIn() = !query("/faces/secured/main.jsp").headers.contains("Location")

    override suspend fun query(path: String, parameters: Parameters?) = httpClient.get(newRequestBuilder(path, parameters))

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
        const val ENDPOINT = "https://objednavky.jidelnasokolska.cz"
    }
}