package me.tomasan7.jecnaapi.web.jecna

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import me.tomasan7.jecnaapi.web.Auth
import me.tomasan7.jecnaapi.web.AuthWebClient
import me.tomasan7.jecnaapi.web.AuthenticationException
import org.jsoup.Jsoup
import java.time.Instant

/**
 * Http client for accessing the Ječná web.
 *
 * @param autoLogin Saves [Auth] that led to successful [login] result.
 * Then when calling [query] and it fails because of [AuthenticationException], [login] is called with the saved [Auth] and the request retried.
 * If it fails again, [AuthenticationException] is thrown.
 */
class JecnaWebClient(var autoLogin: Boolean = false) : AuthWebClient
{
    private val cookieStorage = AcceptAllCookiesStorage()
    private val httpClient = HttpClient(CIO) {
        install(HttpCookies) {
            storage = cookieStorage
        }

        followRedirects = false
    }
    private var token3: String? = null
    private var autoLoginAttempted = false

    var lastSuccessfulLoginAuth: Auth? = null
        private set
    var lastSuccessfulLoginTime: Instant? = null
        private set
    var role: Role? = null
        private set

    suspend fun getCookieValue(name: String) = getCookie(name)?.value

    suspend fun setCookie(name: String, value: String) = cookieStorage.addCookie(ENDPOINT, Cookie(name, value))

    suspend fun getCookie(name: String) = cookieStorage.get(Url(ENDPOINT)).firstOrNull { it.name == name }

    suspend fun getSessionCookie() = getCookie(SESSION_ID_COOKIE_NAME)

    override suspend fun login(auth: Auth): Boolean
    {
        if (token3 == null)
            requestToken3()

        val response = httpClient.submitForm(
            block = newRequestBuilder("/user/login"),
            formParameters = Parameters.build {
                append("user", auth.username)
                append("pass", auth.password)
                append("token3", token3!!)
            })

        if (response.status != HttpStatusCode.Found)
            return false

        val locationHeader = response.headers[HttpHeaders.Location] ?: return false

        if (locationHeader != "/")
            return false

        lastSuccessfulLoginAuth = auth
        lastSuccessfulLoginTime = Instant.now()

        return true
    }

    suspend fun setRole(role: Role)
    {
        plainQuery("/user/role", Parameters.build { append("role", role.value) })
        this.role = role
    }

    override suspend fun logout()
    {
        lastSuccessfulLoginAuth = null
        plainQuery("/user/logout")
    }

    /* Responds with status 302 (redirect to login page) when user is not logged in. */
    override suspend fun isLoggedIn() = plainQuery(LOGIN_TEST_ENDPOINT).status == HttpStatusCode.OK

    /** A query without any authentication (autologin) handling. */
    suspend fun plainQuery(path: String, parameters: Parameters? = null): HttpResponse
    {
        val response = httpClient.get(newRequestBuilder(path, parameters))
        tryFindAndSaveToken3(response.bodyAsText())
        return response
    }

    /** A query without any authentication (autologin) handling. */
    suspend fun plainQueryStringBody(path: String, parameters: Parameters? = null) =
        plainQuery(path, parameters).bodyAsText()

    /**
     * A query with autologin handling.
     *
     * @throws AuthenticationException If the request fails because of authentication. (even after autologin)
     */
    override suspend fun query(path: String, parameters: Parameters?): HttpResponse
    {
        val response = plainQuery(path, parameters)

        /* No redirect to login. */
        val locationHeader = response.headers[HttpHeaders.Location] ?: return response.also { autoLoginAttempted = false }

        if (!locationHeader.startsWith("$ENDPOINT/user/need-login"))
            return response.also { autoLoginAttempted = false }

        /* Redirect to login. */

        if (!autoLogin || lastSuccessfulLoginAuth == null)
            throw AuthenticationException()

        if (autoLoginAttempted)
        {
            autoLoginAttempted = false
            throw AuthenticationException()
        }

        login(lastSuccessfulLoginAuth!!)

        autoLoginAttempted = true
        return query(path, parameters)
    }

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
        val previousRole = role
        /* Login form with the token3 is not in the root page, when you are neither student nor teacher. */
        if (previousRole != Role.STUDENT)
            setRole(Role.STUDENT)

        val foundToken = tryFindAndSaveToken3(plainQueryStringBody("/"))

        if (!foundToken)
            throw RuntimeException("Failed to find token3.")

        if (previousRole != null && previousRole != Role.STUDENT)
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
        }
    }

    companion object
    {
        const val ENDPOINT = "https://www.spsejecna.cz"

        const val SESSION_ID_COOKIE_NAME = "JSESSIONID"

        /**
         * Endpoint used for testing whether user is logged in or not.
         * Using particularly this one, because it's the smallest => fastest to download.
         */
        const val LOGIN_TEST_ENDPOINT = "/user-student/record-list"

        /**
         * Returns the full URL for the given path.
         * @param path The path to query. Must include first slash.
         */
        fun getUrlForPath(path: String) = ENDPOINT + path
    }
}