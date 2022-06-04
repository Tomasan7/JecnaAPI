package me.tomasan7.jecnaapi.web

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.statement.*
import io.ktor.http.*

abstract class AuthWebClient
{
    protected val auth: Auth

    protected val cookieStorage = AcceptAllCookiesStorage()

    protected val httpClient = HttpClient(CIO) {
        install(HttpCookies) {
            storage = cookieStorage
        }
    }

    constructor(auth: Auth)
    {
        this.auth = auth
    }

    constructor(username: String, password: String)
    {
        auth = Auth(username, password)
    }

    /**
     * Logins the client.
     *
     * @return True if login was successful, false otherwise.
     */
    abstract suspend fun login(): Boolean

    /**
     * Makes a request to the provided path. May vary depending on whether user is logged in or not.
     *
     * @param path Relative path from the domain. Must include first slash.
     * @param parameters HTTP parameters, which will be sent URL encoded.
     * @return The HTTP response's body as [String].
     */
    suspend fun queryStringBody(path: String, parameters: Parameters? = null) = query(path, parameters).body<String>()

    /**
     * Makes a request to the provided path. May vary depending on whether user is logged in or not.
     *
     * @param path Relative path from the domain. Must include first slash.
     * @param parameters HTTP parameters, which will be sent URL encoded.
     * @return The [HttpResponse].
     */
    abstract suspend fun query(path: String, parameters: Parameters? = null): HttpResponse
}

/**
 * [append] extension function on [ParametersBuilder], that takes [Pair] as a parameter.
 */
fun ParametersBuilder.append(pair: Pair<String, Any>) = append(pair.first, pair.second.toString())