package com.finalproject.ecommerce.ecommerce.orderspayments.domain.model.commands;

public record MarkOrderAsPaidCommand(String stripeSessionId) {
}

