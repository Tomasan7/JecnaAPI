package me.tomasan7.jecnaapi.web

import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*

class JecnaWebClient : AuthWebClient
{
    constructor(auth: Auth) : super(auth)

    constructor(username: String, password: String) : super(username, password)

    override suspend fun login(): Boolean
    {
        /* The user login request. */
        return httpClient.submitForm(
            block = newRequestBuilder("/user/login"),
            formParameters = Parameters.build {
                append("user", auth.username)
                append("pass", auth.password)
                /* If the login was successful, web responds with a redirect status code. */
            }).status == HttpStatusCode.Found
    }

    override suspend fun query(path: String, parameters: Parameters?) = httpClient.get(newRequestBuilder(path, parameters))

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
    private fun newRequestBuilder(path: String,
                                  parameters: Parameters? = null,
                                  block: (HttpRequestBuilder.() -> Unit)? = null): HttpRequestBuilder.() -> Unit
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
    }
}