package me.tomasan7.jecnaapi.web.canteen

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import me.tomasan7.jecnaapi.parser.HtmlElementNotFoundException
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
        defaultRequest {
            url {
                takeFrom("$ENDPOINT/$CANTEEN_CODE/")
                parameters.append("terminal", "false")
                parameters.append("printer", "false")
                parameters.append("keyboard", "false")
                parameters.append("status", "true")
            }
        }
        followRedirects = false
    }

    suspend fun getCsrfTokenFromCookie() = cookieStorage.get(Url("$ENDPOINT/$CANTEEN_CODE"))["XSRF-TOKEN"]?.value

    /** Tries to find a value of any `input` tag with name `_csrf`. */
    suspend fun findCsrfToken(html: String) = Jsoup
        .parse(html)
        .selectFirst("input[name=_csrf]")
        ?.attr("value")

    fun HttpResponse.isRedirect() = status.value in 300..399

    suspend fun findCsrfTokenOrThrow(html: String) = findCsrfToken(html)
        ?: throw HtmlElementNotFoundException.byName("CSRF token")

    override suspend fun login(auth: Auth): Boolean
    {
        val loginFormHtmlResponse = queryStringBody("login")
        val csrfToken = findCsrfTokenOrThrow(loginFormHtmlResponse)

        val loginPostResponse = httpClient.submitForm(
            url = "j_spring_security_check",
            formParameters = Parameters.build {
                append("j_username", auth.username)
                append("j_password", auth.password)
                append("type", "web")
                append("_csrf", csrfToken)
                append("targetUrl", "/")
            })

        return !loginPostResponse.headers[HttpHeaders.Location]!!.contains("login_error=1")
    }

    override suspend fun logout()
    {
        val csrfToken = getCsrfTokenFromCookie() ?: run {
            val response = query("faces/secured/main.jsp")
            /* Redirects to login, if no one is logged in */
            if (response.isRedirect())
                return

            findCsrfTokenOrThrow(response.bodyAsText())
        }

        httpClient.submitForm("logout", parametersOf("_csrf", csrfToken))
        httpClient.get("logoutall")
    }

    override suspend fun isLoggedIn() = !query("faces/secured/main.jsp").isRedirect()

    override suspend fun query(path: String, parameters: Parameters?) =
        httpClient.get(path) {
            if (parameters != null)
                url.parameters.appendAll(parameters)
        }

    companion object
    {
        const val ENDPOINT = "https://strav.nasejidelna.cz"
        const val CANTEEN_CODE = "0341"
    }
}
