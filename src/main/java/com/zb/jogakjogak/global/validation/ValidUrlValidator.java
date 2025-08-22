package com.zb.jogakjogak.global.validation;

import jakarta.annotation.PostConstruct;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.apache.commons.validator.routines.UrlValidator;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Component
public class ValidUrlValidator implements ConstraintValidator<ValidUrl, String> {

    private static final String[] ALLOWED_SCHEMES = {"http", "https"};
    private final UrlValidator urlValidator = new UrlValidator(ALLOWED_SCHEMES, UrlValidator.NO_FRAGMENTS);

    private static final Set<String> VALID_TLDS = new HashSet<>();

    private final ResourceLoader resourceLoader;

    public ValidUrlValidator(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() {
        try {
            Resource resource = resourceLoader.getResource("classpath:tlds-allowed.txt");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    VALID_TLDS.add(line.trim().toLowerCase());
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load allowed TLDs: " + e.getMessage());
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return false;
        if (value.trim().isEmpty()) return true;

        if (!urlValidator.isValid(value)) return false;

        try {
            String host = new URL(value).getHost();
            String[] parts = host.split("\\.");
            if (parts.length < 2) return false;

            String tld = parts[parts.length - 1].toLowerCase();
            String secondLevel = parts[parts.length - 2].toLowerCase();
            String combined = secondLevel + "." + tld;

            return VALID_TLDS.contains(tld) || VALID_TLDS.contains(combined);
        } catch (Exception e) {
            return false;
        }
    }
}
