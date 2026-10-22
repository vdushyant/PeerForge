# Email verification and password recovery

## Current mode

Email verification and password reset are temporarily disabled by default. New registrations are marked verified and can sign in immediately. Password reset is unavailable until email delivery is enabled; users who lose access need to contact support.

The toggle is `PEERFORGE_EMAIL_ENABLED` and defaults to `false`. With it disabled, the application does not require SMTP configuration, does not send verification emails, and the forgot-password/resend-verification requests do not send messages.

## Re-enable email flows later

Set `PEERFORGE_EMAIL_ENABLED=true`, restore this Spring Mail configuration, and set the environment variables below in the local run configuration and production service environment:

```yaml
spring:
  mail:
    host: ${MAIL_HOST}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail.smtp.auth: ${MAIL_SMTP_AUTH:true}
      mail.smtp.starttls.enable: ${MAIL_SMTP_STARTTLS:true}

peerforge:
  email:
    from: ${MAIL_FROM:}
    base-url: ${APP_BASE_URL:http://localhost:5173}
```

Required settings:

```text
MAIL_HOST=<SMTP host>
MAIL_PORT=587
MAIL_USERNAME=<SMTP username>
MAIL_PASSWORD=<SMTP password>
MAIL_FROM=<verified sender address, if different from username>
MAIL_SMTP_AUTH=true
MAIL_SMTP_STARTTLS=true
APP_BASE_URL=<frontend URL>
```

Never commit real mail credentials. Email verification links expire after 24 hours. Password-reset links expire after 20 minutes, are single-use, and the stored token is hashed. A successful password reset revokes the user's refresh tokens.

## Endpoints

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/verify-email`
- `POST /api/v1/auth/resend-verification`
- `POST /api/v1/auth/forgot-password`
- `POST /api/v1/auth/reset-password`
