package me.tomasan7.jecnaapi.web

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import me.tomasan7.jecnaapi.parser.parsers.selectFirstOrThrow
import org.jsoup.Jsoup
import java.io.File

/**
 * Http client for accessing the Ječná web.
 *
 * @param autoLogin Saves provided [Auth] on each [login] call.
 * Then when calling [query] and it fails because of [AuthenticationException], [login] is called with the saved [Auth] and the request retried.
 * If it fails again, [AuthenticationException] is thrown.
 */
class JecnaWebClient(var autoLogin: Boolean = false) : AuthWebClient
{
    var lastLoginAuth: Auth? = null
        private set

    private val cookieStorage = AcceptAllCookiesStorage()

    private val httpClient = HttpClient(CIO) {
        install(HttpCookies) {
            storage = cookieStorage
        }

        followRedirects = false
    }

    private var token3: String? = null

    suspend fun getCookie(name: String) = cookieStorage.get(Url(ENDPOINT)).firstOrNull { it.name == name }?.value

    suspend fun setCookie(name: String, value: String) = cookieStorage.addCookie(ENDPOINT, Cookie(name, value))

    override suspend fun login(auth: Auth): Boolean
    {
        lastLoginAuth = auth

        if (token3 == null)
            requestToken3()

        val response = httpClient.submitForm(
            block = newRequestBuilder("/user/login") { header(HttpHeaders.Referrer, LOGIN_TEST_REFERER) },
            formParameters = Parameters.build {
                append("user", auth.username)
                append("pass", auth.password)
                append("token3", token3!!)
            })

        if (response.status != HttpStatusCode.Found)
            return false

        val locationHeader = response.headers[HttpHeaders.Location] ?: return false

        return locationHeader == LOGIN_TEST_REFERER
    }

    override suspend fun logout()
    {
        query("/user/logout")
    }

    /* Responds with status 302 (redirect to login page) when user is not logged in. */
    override suspend fun isLoggedIn() = plainQuery(LOGIN_TEST_ENDPOINT).status == HttpStatusCode.OK

    suspend fun plainQuery(path: String, parameters: Parameters? = null) = httpClient.get(newRequestBuilder(path, parameters))

    override suspend fun query(path: String, parameters: Parameters?): HttpResponse
    {
        val response = httpClient.get(newRequestBuilder(path, parameters))

        tryFindAndSaveToken3(response.bodyAsText())

        /* No redirect to login. */
        val locationHeader = response.headers[HttpHeaders.Location] ?: return response

        /* Redirect to login. */
        if (locationHeader.startsWith("$ENDPOINT/user/need-login"))
        {
            if (autoLogin && lastLoginAuth != null)
            {
                /* Login and retry request. */
                login(lastLoginAuth!!)
                /* Throws AuthenticationException if the request still fails because of Auth. */
                return query(path, parameters)
            }
            else
                /* AutoLogin not provided, throwing exception.  */
                throw AuthenticationException()
        }

        return response
    }

    /** Gets user's role cookie. Doesn't make any requests. */
    suspend fun getRole() = getCookie("role")

    /** Sets user's role cookie. Doesn't make any requests. */
    suspend fun setRole(role: String) = setCookie("role", role)

    private fun tryFindAndSaveToken3(htmlDocument: String): Boolean
    {
        val document = Jsoup.parse(htmlDocument)
        val token3Ele = document.selectFirst("input[name=token3]") ?: return false
        val token3 = token3Ele.attr("value")
        this.token3 = token3
        return true
    }

    private suspend fun requestToken3()
    {
        val previousRole = getRole()
        /* Login form with the token3 is not in the root page, when you are neither student nor teacher. */
        if (previousRole != "student")
            setRole("student")

        val foundToken = tryFindAndSaveToken3(queryStringBody("/"))

        if (!foundToken)
            throw RuntimeException("Failed to find token3.")

        if (previousRole != null && previousRole != "student")
            setRole(previousRole)
    }

    /**
     * Closes the HTTP client.
     */
    fun close() = httpClient.close()

    /**
     * Returns a function modifying [HttpRequestBuilder] used by Ktor HttpClient.
     * Sets the url relative to [ENDPOINT].
     * Adds a User-Agent header, since the web requires it. (uses Mozilla/5.0)
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

            /* The web requires a User-Agent header, otherwise it responds to the login request with
			 * 403 - "The page you were looking for is not availible." (yes, it contains the grammar mistake) */
            header(HttpHeaders.UserAgent, "Mozilla/5.0")
        }
    }

    companion object
    {
        const val ENDPOINT = "https://www.spsejecna.cz"

        /** Used as a `Referer` header value in login request, so when server responds, we can check,
         * if it redirects to that location. Note, that it is just this string, no domain. */
        private const val LOGIN_TEST_REFERER = "/LOGIN-SUCCESSFUL"

        /**
         * Endpoint used for testing whether user is logged in or not.
         * Using particularly this one, because it's the smallest => fastest to download.
         */
        const val LOGIN_TEST_ENDPOINT = "/user-student/record-list"
    }
}