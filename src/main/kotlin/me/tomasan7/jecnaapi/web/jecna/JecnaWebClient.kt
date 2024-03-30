package me.tomasan7.jecnaapi.web.jecna

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
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
import kotlin.time.Duration

/**
 * Http client for accessing the Ječná web.
 *
 * @param autoLogin Saves [Auth] that led to successful [login] result.
 * Then when calling [query] and it fails because of [AuthenticationException], [login] is called with the saved [Auth] and the request retried.
 * If it fails again, [AuthenticationException] is thrown.
 */
class JecnaWebClient(var autoLogin: Boolean = false, requestTimeout: Duration) : AuthWebClient
{
    private val cookieStorage = AcceptAllCookiesStorage()
    private val httpClient = HttpClient(CIO) {
        install(HttpCookies) {
            storage = cookieStorage
        }
        install(DefaultRequest) {
            url(ENDPOINT)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = requestTimeout.inWholeMilliseconds
        }
        followRedirects = false
    }
    private var autoLoginAttempted = false

    /**
     * [Auth] used by [autoLogin]. Is automatically updated by [login] on a successful login.
     * Is set to `null` on [logout].
     */
    var autoLoginAuth: Auth? = null
    var lastSuccessfulLoginTime: Instant? = null
        private set
    /* Value may be incorrect if the session has expired */
    var role: Role? = null
        private set

    suspend fun getCookieValue(name: String) = getCookie(name)?.value

    suspend fun setCookie(name: String, value: String) = cookieStorage.addCookie(ENDPOINT, Cookie(name, value))

    suspend fun getCookie(name: String) = cookieStorage.get(Url(ENDPOINT)).firstOrNull { it.name == name }

    suspend fun getSessionCookie() = getCookie(SESSION_ID_COOKIE_NAME)

    override suspend fun login(auth: Auth): Boolean
    {
        val token3 = requestToken3()
            ?: throw IllegalStateException("Token3 not found.")

        val response = httpClient.submitForm(
            "/user/login",
            formParameters = Parameters.build {
                append("user", auth.username)
                append("pass", auth.password)
                append("token3", token3)
            }
        )

        if (response.status != HttpStatusCode.Found)
            return false

        val locationHeader = response.headers[HttpHeaders.Location] ?: return false

        if (locationHeader != "/")
            return false

        autoLoginAuth = auth
        lastSuccessfulLoginTime = Instant.now()

        return true
    }

    suspend fun setRole(role: Role)
    {
        plainQuery("/user/role", parametersOf("role", role.value))
        this.role = role
    }

    override suspend fun logout()
    {
        autoLoginAuth = null
        plainQuery("/user/logout")
    }

    /* Responds with status 302 (redirect to login page) when user is not logged in. */
    override suspend fun isLoggedIn() = plainQuery(LOGIN_TEST_ENDPOINT).status == HttpStatusCode.OK

    /** A query without any authentication (autologin) handling. */
    suspend fun plainQuery(path: String, parameters: Parameters? = null): HttpResponse
    {
        val response = httpClient.get(path) {
            parameters?.let { url.parameters.appendAll(it) }
        }
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

        if (!autoLogin || autoLoginAuth == null)
            throw AuthenticationException()

        if (autoLoginAttempted)
        {
            autoLoginAttempted = false
            throw AuthenticationException()
        }

        login(autoLoginAuth!!)

        autoLoginAttempted = true
        return query(path, parameters)
    }

    private fun findToken3(htmlDocument: String): String?
    {
        val document = Jsoup.parse(htmlDocument)
        val token3Ele = document.selectFirst("input[name=token3]") ?: return null
        return token3Ele.attr("value")
    }

    private suspend fun requestToken3(): String?
    {
        /* May also just manually set the WTDGUID cookie. So save one request.
        However, that's more error-prone, for when they change it. */

        val previousRole = role
        /* Login form with the token3 is not in the root page, when you are neither student nor teacher. */
        setRole(Role.STUDENT)

        val token3 = findToken3(plainQueryStringBody("/"))

        if (previousRole != null && previousRole != Role.STUDENT)
            setRole(previousRole)

        return token3
    }

    /**
     * Closes the HTTP client.
     */
    fun close() = httpClient.close()

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
