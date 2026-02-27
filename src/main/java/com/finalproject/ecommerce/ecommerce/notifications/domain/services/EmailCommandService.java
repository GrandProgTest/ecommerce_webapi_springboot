package com.finalproject.ecommerce.ecommerce.notifications.domain.services;

import com.finalproject.ecommerce.ecommerce.notifications.domain.model.commands.SendEmailCommand;

public interface EmailCommandService {

    void handle(SendEmailCommand command);
}


