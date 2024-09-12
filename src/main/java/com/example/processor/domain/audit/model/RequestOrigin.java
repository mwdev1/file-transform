package com.example.processor.domain.audit.model;

public record RequestOrigin(
        String ipAddress,
        String country,
        String countryCode,
        String isp,
        String org
) {}

