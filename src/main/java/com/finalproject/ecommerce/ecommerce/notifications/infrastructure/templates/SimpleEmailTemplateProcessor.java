package com.finalproject.ecommerce.ecommerce.notifications.infrastructure.templates;

import com.finalproject.ecommerce.ecommerce.notifications.domain.exceptions.TemplateNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Component
public class SimpleEmailTemplateProcessor implements EmailTemplateProcessor {

    private static final String TEMPLATE_BASE_PATH = "templates/emails/";
    private static final String TEMPLATE_EXTENSION = ".html";

    @Override
    public String processTemplate(String templateName, Map<String, Object> data) {
        try {
            String templatePath = TEMPLATE_BASE_PATH + templateName + TEMPLATE_EXTENSION;
            ClassPathResource resource = new ClassPathResource(templatePath);

            if (!resource.exists()) {
                log.error("Template not found: {}", templatePath);
                throw new TemplateNotFoundException(templateName);
            }

            String template = new String(
                    resource.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            String processedTemplate = template;
            if (data != null) {
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    String placeholder = "{{" + entry.getKey() + "}}";
                    String value = entry.getValue() != null ? entry.getValue().toString() : "";
                    processedTemplate = processedTemplate.replace(placeholder, value);
                }
            }

            log.debug("Template {} processed successfully", templateName);
            return processedTemplate;

        } catch (IOException e) {
            log.error("Error reading template {}: {}", templateName, e.getMessage(), e);
            throw new TemplateNotFoundException(templateName);
        }
    }
}

