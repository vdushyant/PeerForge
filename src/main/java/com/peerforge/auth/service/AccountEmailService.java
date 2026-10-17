package com.peerforge.auth.service;

import com.peerforge.common.exception.MailDeliveryUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountEmailService {

    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${peerforge.email.from:}")
    private String configuredFrom;

    @Value("${peerforge.email.base-url:http://localhost:5173}")
    private String frontendBaseUrl;

    public void sendVerificationEmail(String email, String firstName, String rawToken) {
        String verificationLink = frontendBaseUrl.replaceAll("/+$", "")
                + "/verify-email?token=" + rawToken;
        send(email, "Verify your PeerForge email",
                "Hi " + firstName + ",\n\n"
                        + "Verify your email address to activate your PeerForge account:\n"
                        + verificationLink + "\n\n"
                        + "This link expires in 24 hours. If you did not create this account, you can ignore this email.");
    }

    public void sendPasswordResetEmail(String email, String firstName, String rawToken) {
        String resetLink = frontendBaseUrl.replaceAll("/+$", "")
                + "/reset-password?token=" + rawToken;
        send(email, "Reset your PeerForge password",
                "Hi " + firstName + ",\n\n"
                        + "Use this one-time link to choose a new PeerForge password:\n"
                        + resetLink + "\n\n"
                        + "This link expires in 20 minutes. If you did not request a reset, you can ignore this email.");
    }

    private void send(String recipient, String subject, String body) {
        if (mailHost == null || mailHost.isBlank()) {
            throw new MailDeliveryUnavailableException();
        }
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            throw new MailDeliveryUnavailableException();
        }

        SimpleMailMessage message = new SimpleMailMessage();
        String from = configuredFrom == null || configuredFrom.isBlank() ? mailUsername : configuredFrom;
        if (from != null && !from.isBlank()) {
            message.setFrom(from);
        }
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(body);
        try {
            mailSender.send(message);
        } catch (MailException exception) {
            throw new MailDeliveryUnavailableException();
        }
    }
}
