# General

The server uses Apache server.

## Session

A session is started with a first request to the server. (any request)  
Its end is dictated by the browser. (See [Session](https://developer.mozilla.org/en-US/docs/Web/HTTP/Cookies#define_the_lifetime_of_a_cookie))

### Session ID cookie

- A session id is indicated by the `JSESSIONID` cookie. (example value: `ucid34sfo72nmfet9aitildblp4psbmm`)
- It has a lifetime (`Exipres` attribute) of ["Session"](https://developer.mozilla.org/en-US/docs/Web/HTTP/Cookies#define_the_lifetime_of_a_cookie).
- If the cookie is not present yet, any request to the server will create a new session and set the cookie.

# Login

## Request
Path: `/user/login`  
Method: `POST`    
Content-Type: `application/x-www-form-urlencoded`  
Parameters:

| Name     | Required | Description                                               | Example values            |
|----------|----------|-----------------------------------------------------------|---------------------------|
| `user`   | Yes      | User's username, also accepts full email.                 | hula, hula@spsejecna.cz   |
| `pass`   | Yes      | User's password.                                          | qwerty1234                |
| `token3` | Yes      | Acquired with the login form. More info [below](#token3). | 51565081                  |
| `submit` | No       | Sent by the button. Has only value "Přihlásit se"         | Přihlásit se (only value) |

### token3
Token3 is a [CSRF Protection](https://laravel.com/docs/9.x/csrf). It is always tied to a session, so each session has the same token3 for its entire life. It has a numeric value. You can find it as a hidden `input` in the login form. A selector would be `#loginForm input[name=token3]`.

## Response
- If the password or username is incorrect, the server will respond (`200 OK`) with a login problem page. (same as `/user/login-problem`)  
- If token3 is missing or incorrect, the user will be redirected (`302 Moved Temporarily`) to `/user/login-problem`.
- If the login is successful, the user will be redirected (`302 Moved Temporarily`) to either:
  - the page, which the login happened from. ([Referer header](https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Referer))
  - root (`/`) in case the user was already logged in when the login happened.