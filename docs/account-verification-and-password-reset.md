# Email verification and password recovery

## User flow

- New registrations are saved as unverified and receive a 24-hour verification link. They cannot sign in until the link is redeemed.
- The login screen's “Forgot Password?” link opens the password recovery form. A verified account receives a 20-minute, single-use reset link.
- Password-reset and verification tokens are generated randomly; only SHA-256 hashes are stored in PostgreSQL.
- Password reset replaces the password hash and revokes the user's refresh tokens.
- Forgot-password and resend-verification requests return generic messages so the API does not reveal whether an address has an account.
- Migration V19 marks existing accounts as verified so current users are not locked out.

Email verification confirms control of an address when the link is used. It does not prove the address will remain valid, and some disposable inboxes can receive verification mail. Domain blocking and request throttling can be added as anti-abuse measures if needed.

## Configure SMTP

The application uses Spring Mail with environment variables. No credentials are stored in the repository:

```text
MAIL_HOST=<SMTP host>
MAIL_PORT=587
MAIL_USERNAME=<SMTP username>
MAIL_PASSWORD=<SMTP password>
MAIL_FROM=<verified sender address, if different from username>
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true
APP_BASE_URL=https://d1mxko2b62xlbg.cloudfront.net
```

For local development, set `APP_BASE_URL=http://localhost:5173`. Configure the variables in IntelliJ's run configuration locally and in the backend service environment in production. Registration returns a service-unavailable response when SMTP is not configured, instead of creating an account that cannot be verified.

## Endpoints

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/verify-email`
- `POST /api/v1/auth/resend-verification`
- `POST /api/v1/auth/forgot-password`
- `POST /api/v1/auth/reset-password`
