package com.finalproject.ecommerce.ecommerce.notifications.application.internal.commandservices;

import com.finalproject.ecommerce.ecommerce.notifications.domain.model.commands.SendEmailCommand;
import com.finalproject.ecommerce.ecommerce.notifications.domain.model.valueobjects.EmailTemplate;
import com.finalproject.ecommerce.ecommerce.notifications.infrastructure.email.EmailProvider;
import com.finalproject.ecommerce.ecommerce.notifications.infrastructure.templates.EmailTemplateProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailCommandServiceImpl Tests")
class EmailCommandServiceImplTest {

    @Mock
    private EmailProvider emailProvider;

    @Mock
    private EmailTemplateProcessor templateEngine;

    @InjectMocks
    private EmailCommandServiceImpl emailCommandService;

    private Map<String, Object> templateData;

    @BeforeEach
    void setUp() {
        templateData = new HashMap<>();
        templateData.put("username", "testuser");
        templateData.put("activationLink", "http://example.com/activate");
    }

    @Test
    @DisplayName("Should send email successfully")
    void shouldSendEmailSuccessfully() {
        String toEmail = "test@example.com";
        String subject = "Welcome!";
        EmailTemplate template = EmailTemplate.WELCOME;
        String processedHtml = "<html>Welcome testuser</html>";

        when(templateEngine.processTemplate(eq(template.getTemplateName()), eq(templateData)))
                .thenReturn(processedHtml);
        when(emailProvider.sendEmail(eq(toEmail), eq(subject), eq(processedHtml)))
                .thenReturn(true);

        SendEmailCommand command = new SendEmailCommand(toEmail, template, templateData, subject);

        emailCommandService.handle(command);

        verify(templateEngine).processTemplate(template.getTemplateName(), templateData);
        verify(emailProvider).sendEmail(toEmail, subject, processedHtml);
    }

    @Test
    @DisplayName("Should handle email sending failure gracefully")
    void shouldHandleEmailSendingFailureGracefully() {
        String toEmail = "test@example.com";
        String subject = "Test Subject";
        EmailTemplate template = EmailTemplate.PASSWORD_RESET;
        String processedHtml = "<html>Reset password</html>";

        when(templateEngine.processTemplate(eq(template.getTemplateName()), eq(templateData)))
                .thenReturn(processedHtml);
        when(emailProvider.sendEmail(eq(toEmail), eq(subject), eq(processedHtml)))
                .thenReturn(false);

        SendEmailCommand command = new SendEmailCommand(toEmail, template, templateData, subject);

        emailCommandService.handle(command);

        verify(templateEngine).processTemplate(template.getTemplateName(), templateData);
        verify(emailProvider).sendEmail(toEmail, subject, processedHtml);
    }

    @Test
    @DisplayName("Should handle template processing exception")
    void shouldHandleTemplateProcessingException() {
        String toEmail = "test@example.com";
        String subject = "Test Subject";
        EmailTemplate template = EmailTemplate.WELCOME;

        when(templateEngine.processTemplate(eq(template.getTemplateName()), eq(templateData)))
                .thenThrow(new RuntimeException("Template processing error"));

        SendEmailCommand command = new SendEmailCommand(toEmail, template, templateData, subject);

        emailCommandService.handle(command);

        verify(templateEngine).processTemplate(template.getTemplateName(), templateData);
        verify(emailProvider, never()).sendEmail(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should handle email provider exception")
    void shouldHandleEmailProviderException() {
        String toEmail = "test@example.com";
        String subject = "Test Subject";
        EmailTemplate template = EmailTemplate.PASSWORD_CHANGED;
        String processedHtml = "<html>Password changed</html>";

        when(templateEngine.processTemplate(eq(template.getTemplateName()), eq(templateData)))
                .thenReturn(processedHtml);
        when(emailProvider.sendEmail(eq(toEmail), eq(subject), eq(processedHtml)))
                .thenThrow(new RuntimeException("Email provider error"));

        SendEmailCommand command = new SendEmailCommand(toEmail, template, templateData, subject);

        emailCommandService.handle(command);

        verify(templateEngine).processTemplate(template.getTemplateName(), templateData);
        verify(emailProvider).sendEmail(toEmail, subject, processedHtml);
    }

    @Test
    @DisplayName("Should send batch email successfully")
    void shouldSendBatchEmailSuccessfully() throws Exception {
        Set<String> recipients = Set.of("user1@example.com", "user2@example.com", "user3@example.com");
        String subject = "Discount Alert";
        EmailTemplate template = EmailTemplate.DISCOUNT_ALERT;
        String processedHtml = "<html>Discount available</html>";

        when(templateEngine.processTemplate(eq(template.getTemplateName()), eq(templateData)))
                .thenReturn(processedHtml);
        when(emailProvider.sendBatchEmail(eq(recipients), eq(subject), eq(processedHtml)))
                .thenReturn(true);

        CompletableFuture<Void> result = emailCommandService.sendBatchEmailAsync(
                recipients, template, subject, templateData);

        assertThat(result).isNotNull();
        result.get();

        verify(templateEngine).processTemplate(template.getTemplateName(), templateData);
        verify(emailProvider).sendBatchEmail(recipients, subject, processedHtml);
    }

    @Test
    @DisplayName("Should handle batch email failure")
    void shouldHandleBatchEmailFailure() throws Exception {
        Set<String> recipients = Set.of("user1@example.com", "user2@example.com");
        String subject = "Low Stock Alert";
        EmailTemplate template = EmailTemplate.LOW_STOCK_ALERT;
        String processedHtml = "<html>Low stock</html>";

        when(templateEngine.processTemplate(eq(template.getTemplateName()), eq(templateData)))
                .thenReturn(processedHtml);
        when(emailProvider.sendBatchEmail(eq(recipients), eq(subject), eq(processedHtml)))
                .thenReturn(false);

        CompletableFuture<Void> result = emailCommandService.sendBatchEmailAsync(
                recipients, template, subject, templateData);

        assertThat(result).isNotNull();
        result.get();

        verify(templateEngine).processTemplate(template.getTemplateName(), templateData);
        verify(emailProvider).sendBatchEmail(recipients, subject, processedHtml);
    }

    @Test
    @DisplayName("Should handle batch email template exception")
    void shouldHandleBatchEmailTemplateException() throws Exception {
        Set<String> recipients = Set.of("user1@example.com");
        String subject = "Test";
        EmailTemplate template = EmailTemplate.DISCOUNT_ALERT;

        when(templateEngine.processTemplate(eq(template.getTemplateName()), eq(templateData)))
                .thenThrow(new RuntimeException("Template error"));

        CompletableFuture<Void> result = emailCommandService.sendBatchEmailAsync(
                recipients, template, subject, templateData);

        assertThat(result).isNotNull();
        result.get();

        verify(templateEngine).processTemplate(template.getTemplateName(), templateData);
        verify(emailProvider, never()).sendBatchEmail(any(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should handle batch email provider exception")
    void shouldHandleBatchEmailProviderException() throws Exception {
        Set<String> recipients = Set.of("user1@example.com", "user2@example.com");
        String subject = "Alert";
        EmailTemplate template = EmailTemplate.LOW_STOCK_ALERT;
        String processedHtml = "<html>Alert</html>";

        when(templateEngine.processTemplate(eq(template.getTemplateName()), eq(templateData)))
                .thenReturn(processedHtml);
        when(emailProvider.sendBatchEmail(eq(recipients), eq(subject), eq(processedHtml)))
                .thenThrow(new RuntimeException("Provider error"));

        CompletableFuture<Void> result = emailCommandService.sendBatchEmailAsync(
                recipients, template, subject, templateData);

        assertThat(result).isNotNull();
        result.get();

        verify(templateEngine).processTemplate(template.getTemplateName(), templateData);
        verify(emailProvider).sendBatchEmail(recipients, subject, processedHtml);
    }

    @Test
    @DisplayName("Should send email with different templates")
    void shouldSendEmailWithDifferentTemplates() {
        for (EmailTemplate template : EmailTemplate.values()) {
            String toEmail = "test@example.com";
            String subject = "Test " + template.name();
            String processedHtml = "<html>" + template.name() + "</html>";

            reset(templateEngine, emailProvider);

            when(templateEngine.processTemplate(eq(template.getTemplateName()), any()))
                    .thenReturn(processedHtml);
            when(emailProvider.sendEmail(eq(toEmail), eq(subject), eq(processedHtml)))
                    .thenReturn(true);

            SendEmailCommand command = new SendEmailCommand(toEmail, template, templateData, subject);
            emailCommandService.handle(command);

            verify(templateEngine).processTemplate(eq(template.getTemplateName()), any());
        }
    }

    @Test
    @DisplayName("Should handle empty template data")
    void shouldHandleEmptyTemplateData() {
        String toEmail = "test@example.com";
        String subject = "Test";
        EmailTemplate template = EmailTemplate.WELCOME;
        Map<String, Object> emptyData = new HashMap<>();
        String processedHtml = "<html>Welcome</html>";

        when(templateEngine.processTemplate(eq(template.getTemplateName()), eq(emptyData)))
                .thenReturn(processedHtml);
        when(emailProvider.sendEmail(eq(toEmail), eq(subject), eq(processedHtml)))
                .thenReturn(true);

        SendEmailCommand command = new SendEmailCommand(toEmail, template, emptyData, subject);

        emailCommandService.handle(command);

        verify(templateEngine).processTemplate(template.getTemplateName(), emptyData);
        verify(emailProvider).sendEmail(toEmail, subject, processedHtml);
    }

    @Test
    @DisplayName("Should send batch email to single recipient")
    void shouldSendBatchEmailToSingleRecipient() throws Exception {
        Set<String> recipients = Set.of("single@example.com");
        String subject = "Single Recipient Test";
        EmailTemplate template = EmailTemplate.DISCOUNT_ALERT;
        String processedHtml = "<html>Discount</html>";

        when(templateEngine.processTemplate(eq(template.getTemplateName()), eq(templateData)))
                .thenReturn(processedHtml);
        when(emailProvider.sendBatchEmail(eq(recipients), eq(subject), eq(processedHtml)))
                .thenReturn(true);

        CompletableFuture<Void> result = emailCommandService.sendBatchEmailAsync(
                recipients, template, subject, templateData);

        assertThat(result).isNotNull();
        result.get();

        verify(emailProvider).sendBatchEmail(recipients, subject, processedHtml);
    }
}


