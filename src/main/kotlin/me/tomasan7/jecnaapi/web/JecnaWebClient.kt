package me.tomasan7.jecnaapi.web

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import me.tomasan7.jecnaapi.web.JecnaWebClient.AuthenticationException

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

    override suspend fun login(auth: Auth): Boolean
    {
        lastLoginAuth = auth

        /* The user login request. */
        return httpClient.submitForm(
            block = newRequestBuilder("/user/login"),
            formParameters = Parameters.build {
                append("user", auth.username)
                append("pass", auth.password)
                /* If the login was successful, web responds with a redirect status code. */
            }).status == HttpStatusCode.Found
    }

    override suspend fun logout()
    {
        query("/user/logout")
    }

    override suspend fun isLoggedIn(): Boolean
    {
        /* Responds with status 302 (redirect to login page) when user is not logged in. */
        return query(LOGIN_TEST_ENDPOINT).status != HttpStatusCode.Found
    }

    /**
     * @throws AuthenticationException When the query fails because user is not authenticated.
     */
    override suspend fun query(path: String, parameters: Parameters?): HttpResponse
    {
        val response = httpClient.get(newRequestBuilder(path, parameters))

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

    class AuthenticationException : RuntimeException("User has to be authenticated to perform this action.")

    companion object
    {
        const val ENDPOINT = "https://www.spsejecna.cz"

        /**
         * Endpoint used for testing whether user is logged in or not.
         * Using particularly this one, because it's the smallest => fastest to download.
         */
        const val LOGIN_TEST_ENDPOINT = "/user-student/record-list"
    }
}