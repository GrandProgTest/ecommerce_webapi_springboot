package com.finalproject.ecommerce.ecommerce.notifications.infrastructure.templates;

import java.util.Map;

public interface EmailTemplateProcessor {
    String processTemplate(String templateName, Map<String, Object> data);
}

