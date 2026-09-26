package com.la_navaja.backend.infrastructure.adapters.mail;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Component
class EmailTemplateRenderer {

    private static final String EMAIL_TEMPLATE_PREFIX = "emails/";

    private final SpringTemplateEngine templateEngine;

    EmailTemplateRenderer(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    String render(String templateName, Map<String, Object> model) {
        Context context = new Context();
        context.setVariables(model);
        return templateEngine.process(EMAIL_TEMPLATE_PREFIX + templateName, context);
    }
}
