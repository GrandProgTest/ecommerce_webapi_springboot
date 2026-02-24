package com.finalproject.ecommerce.ecommerce.notifications.domain.model.valueobjects;

public enum EmailTemplate {
    PASSWORD_RESET("password-reset", "Password Reset Request"),
    PASSWORD_CHANGED("password-changed", "Password Changed Successfully"),
    WELCOME("welcome", "Welcome to Our Store"),
    ORDER_CONFIRMATION("order-confirmation", "Order Confirmation"),
    ORDER_STATUS_UPDATE("order-status-update", "Order Status Update"),
    ORDER_SHIPPED("order-shipped", "Your Order Has Been Shipped"),
    ORDER_DELIVERED("order-delivered", "Your Order Has Been Delivered"),
    PAYMENT_SUCCESS("payment-success", "Payment Successful"),
    PAYMENT_FAILED("payment-failed", "Payment Failed"),
    LOW_STOCK_ALERT("low-stock-alert", "Product Low Stock Alert"),
    DISCOUNT_ALERT("discount-alert", "Price Drop on a Product You Liked!");

    private final String templateName;
    private final String defaultSubject;

    EmailTemplate(String templateName, String defaultSubject) {
        this.templateName = templateName;
        this.defaultSubject = defaultSubject;
    }

    public String getTemplateName() {
        return templateName;
    }

    public String getDefaultSubject() {
        return defaultSubject;
    }
}

