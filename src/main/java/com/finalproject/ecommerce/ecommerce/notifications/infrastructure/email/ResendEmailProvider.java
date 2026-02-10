package com.finalproject.ecommerce.ecommerce.notifications.infrastructure.email;

import com.finalproject.ecommerce.ecommerce.notifications.domain.exceptions.EmailSendingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Slf4j
@Component
public class ResendEmailProvider implements EmailProvider {

    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    @Value("${resend.api-key}")
    private String apiKey;

    @Value("${resend.from-email:noreply@ecommerce.com}")
    private String fromEmail;

    @Value("${resend.from-name:E-Commerce Store}")
    private String fromName;

    private final HttpClient httpClient;

    public ResendEmailProvider() {
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public boolean sendEmail(String to, String subject, String htmlBody) {
        try {
            String jsonPayload = buildJsonPayload(to, subject, htmlBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(RESEND_API_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                log.info("Email sent successfully via Resend to: {}", to);
                return true;
            } else {
                log.error("Resend API returned error status {}: {}",
                        response.statusCode(), response.body());
                return false;
            }

        } catch (Exception e) {
            log.error("Failed to send email via Resend to {}: {}", to, e.getMessage(), e);
            throw new EmailSendingException("Failed to send email via Resend", e);
        }
    }

    private String buildJsonPayload(String to, String subject, String htmlBody) {
        return String.format("""
                {
                    "from": "%s <%s>",
                    "to": ["%s"],
                    "subject": "%s",
                    "html": %s
                }
                """,
                fromName,
                fromEmail,
                to,
                escapeJson(subject),
                escapeJsonString(htmlBody)
        );
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String escapeJsonString(String input) {
        if (input == null) return "\"\"";
        String escaped = escapeJson(input);
        return "\"" + escaped + "\"";
    }
}

