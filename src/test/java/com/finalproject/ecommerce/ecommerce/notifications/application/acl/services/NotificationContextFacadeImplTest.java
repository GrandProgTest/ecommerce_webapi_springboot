package com.finalproject.ecommerce.ecommerce.notifications.application.acl.services;

import com.finalproject.ecommerce.ecommerce.notifications.application.internal.commandservices.EmailCommandServiceImpl;
import com.finalproject.ecommerce.ecommerce.notifications.domain.model.commands.SendEmailCommand;
import com.finalproject.ecommerce.ecommerce.notifications.domain.model.valueobjects.EmailTemplate;
import com.finalproject.ecommerce.ecommerce.notifications.domain.services.EmailCommandService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationContextFacadeImpl Unit Tests")
class NotificationContextFacadeImplTest {

    @Mock
    private EmailCommandService emailCommandService;

    @Mock
    private EmailCommandServiceImpl emailCommandServiceImpl;

    @InjectMocks
    private NotificationContextFacadeImpl notificationContextFacade;

    @Captor
    private ArgumentCaptor<SendEmailCommand> commandCaptor;

    @Captor
    private ArgumentCaptor<Set<String>> recipientsCaptor;

    @Captor
    private ArgumentCaptor<Map<String, Object>> templateDataCaptor;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_USERNAME = "John Doe";
    private static final String BASE_URL = "http://localhost:8080";

    @BeforeEach
    void setUp() {
        notificationContextFacade = new NotificationContextFacadeImpl(
                emailCommandService,
                emailCommandServiceImpl,
                BASE_URL
        );
    }

    @Test
    @DisplayName("Should send password reset email successfully")
    void testSendPasswordResetEmail_Success() {
        String resetToken = "reset-token-123";
        int expirationMinutes = 30;

        doNothing().when(emailCommandService).handle(any(SendEmailCommand.class));

        notificationContextFacade.sendPasswordResetEmail(TEST_EMAIL, TEST_USERNAME, resetToken, expirationMinutes);

        verify(emailCommandService, times(1)).handle(commandCaptor.capture());

        SendEmailCommand capturedCommand = commandCaptor.getValue();
        assertThat(capturedCommand.toEmail()).isEqualTo(TEST_EMAIL);
        assertThat(capturedCommand.template()).isEqualTo(EmailTemplate.PASSWORD_RESET);
        assertThat(capturedCommand.templateData()).containsEntry("username", TEST_USERNAME);
        assertThat(capturedCommand.templateData()).containsEntry("resetToken", resetToken);
        assertThat(capturedCommand.templateData()).containsEntry("expirationMinutes", String.valueOf(expirationMinutes));
    }

    @Test
    @DisplayName("Should handle exception when sending password reset email")
    void testSendPasswordResetEmail_ExceptionHandled() {
        String resetToken = "reset-token-123";
        int expirationMinutes = 30;

        doThrow(new RuntimeException("Email service unavailable"))
                .when(emailCommandService).handle(any(SendEmailCommand.class));

        notificationContextFacade.sendPasswordResetEmail(TEST_EMAIL, TEST_USERNAME, resetToken, expirationMinutes);

        verify(emailCommandService, times(1)).handle(any(SendEmailCommand.class));
    }

    @Test
    @DisplayName("Should send password changed email successfully")
    void testSendPasswordChangedEmail_Success() {
        String changeDateTime = "2026-03-02 10:30:00";

        doNothing().when(emailCommandService).handle(any(SendEmailCommand.class));

        notificationContextFacade.sendPasswordChangedEmail(TEST_EMAIL, TEST_USERNAME, changeDateTime);

        verify(emailCommandService, times(1)).handle(commandCaptor.capture());

        SendEmailCommand capturedCommand = commandCaptor.getValue();
        assertThat(capturedCommand.toEmail()).isEqualTo(TEST_EMAIL);
        assertThat(capturedCommand.template()).isEqualTo(EmailTemplate.PASSWORD_CHANGED);
        assertThat(capturedCommand.templateData()).containsEntry("userName", TEST_USERNAME);
        assertThat(capturedCommand.templateData()).containsEntry("changeDateTime", changeDateTime);
    }

    @Test
    @DisplayName("Should handle exception when sending password changed email")
    void testSendPasswordChangedEmail_ExceptionHandled() {
        String changeDateTime = "2026-03-02 10:30:00";

        doThrow(new RuntimeException("Email service error"))
                .when(emailCommandService).handle(any(SendEmailCommand.class));

        notificationContextFacade.sendPasswordChangedEmail(TEST_EMAIL, TEST_USERNAME, changeDateTime);

        verify(emailCommandService, times(1)).handle(any(SendEmailCommand.class));
    }

    @Test
    @DisplayName("Should send welcome email successfully")
    void testSendWelcomeEmail_Success() {
        String activationUrl = "https://example.com/activate?token=abc123";

        doNothing().when(emailCommandService).handle(any(SendEmailCommand.class));

        notificationContextFacade.sendWelcomeEmail(TEST_EMAIL, TEST_USERNAME, activationUrl);

        verify(emailCommandService, times(1)).handle(commandCaptor.capture());

        SendEmailCommand capturedCommand = commandCaptor.getValue();
        assertThat(capturedCommand.toEmail()).isEqualTo(TEST_EMAIL);
        assertThat(capturedCommand.template()).isEqualTo(EmailTemplate.WELCOME);
        assertThat(capturedCommand.templateData()).containsEntry("username", TEST_USERNAME);
        assertThat(capturedCommand.templateData()).containsEntry("activationUrl", activationUrl);
    }

    @Test
    @DisplayName("Should handle exception when sending welcome email")
    void testSendWelcomeEmail_ExceptionHandled() {
        String activationUrl = "https://example.com/activate?token=abc123";

        doThrow(new RuntimeException("Email sending failed"))
                .when(emailCommandService).handle(any(SendEmailCommand.class));

        notificationContextFacade.sendWelcomeEmail(TEST_EMAIL, TEST_USERNAME, activationUrl);

        verify(emailCommandService, times(1)).handle(any(SendEmailCommand.class));
    }

    @Test
    @DisplayName("Should send low stock alert batch successfully")
    void testSendLowStockAlertBatch_Success() {
        Set<String> recipientEmails = Set.of("admin1@example.com", "admin2@example.com", "manager@example.com");
        String productName = "Gaming Laptop";
        int currentStock = 5;

        when(emailCommandServiceImpl.sendBatchEmailAsync(any(), any(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        notificationContextFacade.sendLowStockAlertBatch(recipientEmails, productName, currentStock);

        verify(emailCommandServiceImpl, times(1)).sendBatchEmailAsync(
                recipientsCaptor.capture(),
                eq(EmailTemplate.LOW_STOCK_ALERT),
                eq(EmailTemplate.LOW_STOCK_ALERT.getDefaultSubject()),
                templateDataCaptor.capture()
        );

        Set<String> capturedRecipients = recipientsCaptor.getValue();
        Map<String, Object> capturedTemplateData = templateDataCaptor.getValue();

        assertThat(capturedRecipients).containsExactlyInAnyOrderElementsOf(recipientEmails);
        assertThat(capturedTemplateData).containsEntry("productName", productName);
        assertThat(capturedTemplateData).containsEntry("currentStock", String.valueOf(currentStock));
    }

    @Test
    @DisplayName("Should handle exception when sending low stock alert batch")
    void testSendLowStockAlertBatch_ExceptionHandled() {
        Set<String> recipientEmails = Set.of("admin@example.com");
        String productName = "Test Product";
        int currentStock = 3;

        when(emailCommandServiceImpl.sendBatchEmailAsync(any(), any(), anyString(), any()))
                .thenThrow(new RuntimeException("Batch email failed"));

        notificationContextFacade.sendLowStockAlertBatch(recipientEmails, productName, currentStock);

        verify(emailCommandServiceImpl, times(1)).sendBatchEmailAsync(any(), any(), anyString(), any());
    }

    @Test
    @DisplayName("Should send order status update successfully")
    void testSendOrderStatusUpdate_Success() {
        Long orderId = 12345L;
        String orderStatus = "SHIPPED";
        String statusMessage = "Your order has been shipped";
        String totalAmount = "$150.00";
        String orderDate = "2026-03-01";

        doNothing().when(emailCommandService).handle(any(SendEmailCommand.class));

        notificationContextFacade.sendOrderStatusUpdate(
                TEST_EMAIL, TEST_USERNAME, orderId, orderStatus, statusMessage, totalAmount, orderDate
        );

        verify(emailCommandService, times(1)).handle(commandCaptor.capture());

        SendEmailCommand capturedCommand = commandCaptor.getValue();
        assertThat(capturedCommand.toEmail()).isEqualTo(TEST_EMAIL);
        assertThat(capturedCommand.template()).isEqualTo(EmailTemplate.ORDER_STATUS_UPDATE);
        assertThat(capturedCommand.templateData()).containsEntry("username", TEST_USERNAME);
        assertThat(capturedCommand.templateData()).containsEntry("orderId", orderId.toString());
        assertThat(capturedCommand.templateData()).containsEntry("orderStatus", orderStatus);
        assertThat(capturedCommand.templateData()).containsEntry("statusMessage", statusMessage);
        assertThat(capturedCommand.templateData()).containsEntry("totalAmount", totalAmount);
        assertThat(capturedCommand.templateData()).containsEntry("orderDate", orderDate);
        assertThat(capturedCommand.templateData()).containsEntry("orderUrl", BASE_URL + "/api/v1/orders/user/" + orderId);
    }

    @Test
    @DisplayName("Should handle exception when sending order status update")
    void testSendOrderStatusUpdate_ExceptionHandled() {
        Long orderId = 12345L;
        String orderStatus = "DELIVERED";
        String statusMessage = "Your order has been delivered";
        String totalAmount = "$200.00";
        String orderDate = "2026-03-01";

        doThrow(new RuntimeException("Email service error"))
                .when(emailCommandService).handle(any(SendEmailCommand.class));

        notificationContextFacade.sendOrderStatusUpdate(
                TEST_EMAIL, TEST_USERNAME, orderId, orderStatus, statusMessage, totalAmount, orderDate
        );

        verify(emailCommandService, times(1)).handle(any(SendEmailCommand.class));
    }

    @Test
    @DisplayName("Should send discount alert batch successfully")
    void testSendDiscountAlertBatch_Success() {
        Set<String> recipientEmails = Set.of("customer1@example.com", "customer2@example.com");
        String productName = "Mechanical Keyboard";
        String originalPrice = "$150.00";
        String salePrice = "$120.00";
        String savingsPercentage = "20%";
        String salePriceExpireDate = "2026-03-15";

        when(emailCommandServiceImpl.sendBatchEmailAsync(any(), any(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        notificationContextFacade.sendDiscountAlertBatch(
                recipientEmails, productName, originalPrice, salePrice, savingsPercentage, salePriceExpireDate
        );

        verify(emailCommandServiceImpl, times(1)).sendBatchEmailAsync(
                recipientsCaptor.capture(),
                eq(EmailTemplate.DISCOUNT_ALERT),
                eq(EmailTemplate.DISCOUNT_ALERT.getDefaultSubject()),
                templateDataCaptor.capture()
        );

        Set<String> capturedRecipients = recipientsCaptor.getValue();
        Map<String, Object> capturedTemplateData = templateDataCaptor.getValue();

        assertThat(capturedRecipients).containsExactlyInAnyOrderElementsOf(recipientEmails);
        assertThat(capturedTemplateData).containsEntry("productName", productName);
        assertThat(capturedTemplateData).containsEntry("originalPrice", originalPrice);
        assertThat(capturedTemplateData).containsEntry("salePrice", salePrice);
        assertThat(capturedTemplateData).containsEntry("savingsPercentage", savingsPercentage);
        assertThat(capturedTemplateData).containsEntry("salePriceExpireDate", salePriceExpireDate);
    }

    @Test
    @DisplayName("Should handle exception when sending discount alert batch")
    void testSendDiscountAlertBatch_ExceptionHandled() {
        Set<String> recipientEmails = Set.of("customer@example.com");
        String productName = "Test Product";
        String originalPrice = "$100.00";
        String salePrice = "$80.00";
        String savingsPercentage = "20%";
        String salePriceExpireDate = "2026-03-15";

        when(emailCommandServiceImpl.sendBatchEmailAsync(any(), any(), anyString(), any()))
                .thenThrow(new RuntimeException("Batch email service error"));

        notificationContextFacade.sendDiscountAlertBatch(
                recipientEmails, productName, originalPrice, salePrice, savingsPercentage, salePriceExpireDate
        );

        verify(emailCommandServiceImpl, times(1)).sendBatchEmailAsync(any(), any(), anyString(), any());
    }

    @Test
    @DisplayName("Should send low stock alert batch with single recipient")
    void testSendLowStockAlertBatch_SingleRecipient() {
        Set<String> recipientEmails = Set.of("admin@example.com");
        String productName = "Limited Edition Product";
        int currentStock = 1;

        when(emailCommandServiceImpl.sendBatchEmailAsync(any(), any(), anyString(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        notificationContextFacade.sendLowStockAlertBatch(recipientEmails, productName, currentStock);

        verify(emailCommandServiceImpl, times(1)).sendBatchEmailAsync(
                eq(recipientEmails),
                eq(EmailTemplate.LOW_STOCK_ALERT),
                anyString(),
                any()
        );
    }

    @Test
    @DisplayName("Should send order status update with all required fields")
    void testSendOrderStatusUpdate_AllFieldsPresent() {
        Long orderId = 99999L;
        String orderStatus = "PROCESSING";
        String statusMessage = "Your order is being processed";
        String totalAmount = "$999.99";
        String orderDate = "2026-03-02";

        doNothing().when(emailCommandService).handle(any(SendEmailCommand.class));

        notificationContextFacade.sendOrderStatusUpdate(
                TEST_EMAIL, TEST_USERNAME, orderId, orderStatus, statusMessage, totalAmount, orderDate
        );

        verify(emailCommandService, times(1)).handle(commandCaptor.capture());

        SendEmailCommand capturedCommand = commandCaptor.getValue();
        Map<String, Object> templateData = capturedCommand.templateData();

        assertThat(templateData).hasSize(7);
        assertThat(templateData).containsKeys(
                "username", "orderId", "orderStatus", "statusMessage", "totalAmount", "orderDate", "orderUrl"
        );
    }
}

