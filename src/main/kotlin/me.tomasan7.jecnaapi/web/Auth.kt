package me.tomasan7.jecnaapi.web

import java.nio.charset.StandardCharsets

/**
 * Simply holds a username and password pair.
 * Defines [encrypt] and [decrypt] methods.
 */
class Auth(username: String, password: String)
{
    val username: String
    val password: String

    init
    {
        require(username.isNotBlank() && password.isNotBlank()) { "Username and password cannot be empty." }

        val resultUsername = username.trim()
        val resultPassword = password.trim()

        require(!resultUsername.contains("\n") && !resultPassword.contains("\n")) { "Username and password cannot contain new lines." }

        require(resultPassword.matches(VALID_PASSWORD_REGEX.toRegex())) { "Invalid password." }

        this.username = resultUsername
        this.password = resultPassword
    }

    /**
     * Simply encrypts this login into `byte[]` so it isn't easily human-readable.
     * **Doesn't secure the information!**
     *
     * @return The encrypted login.
     */
    fun encrypt(): ByteArray
    {
        val usernameBytes = username.toByteArray(StandardCharsets.UTF_8)
        val passwordBytes = password.toByteArray(StandardCharsets.UTF_8)

        /* The +1 in size is for the new line. (\n) */
        val bytes = ByteArray(usernameBytes.size + passwordBytes.size + 1)

        /* Copies usernameBytes, new line and passwordBytes in order into bytes. */
        System.arraycopy(usernameBytes, 0, bytes, 0, usernameBytes.size)
        bytes[usernameBytes.size] = '\n'.code.toByte()
        System.arraycopy(passwordBytes, 0, bytes, usernameBytes.size + 1, passwordBytes.size)

        /* Shift the bytes, so each character's byte is shifted. */
        for (i in bytes.indices)
            bytes[i] = (bytes[i] + 10).toByte()

        return bytes
    }

    companion object
    {
        /**
         * All passwords are checked with this regex.
         * If the password (trimmed) doesn't match this regex, it's invalid and construction will fail.
         */
        private const val VALID_PASSWORD_REGEX = "[\\w\\-@$!%*#?&.]+"

        /**
         * Decrypt encrypted [Auth].
         * @see .encrypt
         * @param bytes The encrypted bytes.
         * @return The [Auth] instance.
         */
        fun decrypt(bytes: ByteArray): Auth
        {
            /* Shift the characters bytes back. */
            for (i in bytes.indices)
                bytes[i] = (bytes[i] - 10).toByte()

            val asString = String(bytes, StandardCharsets.UTF_8)
            val split = asString.split("\n", limit = 2)

            val username = split[0]
            val password = split[1]

            return Auth(username, password)
        }
    }
}