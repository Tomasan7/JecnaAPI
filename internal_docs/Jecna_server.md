# Login

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