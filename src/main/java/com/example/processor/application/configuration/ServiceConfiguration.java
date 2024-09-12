package com.example.processor.application.configuration;

import com.example.processor.domain.validation.model.ValidationType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(value = "application.service")
public class ServiceConfiguration {

    private List<String> inputAttributes = List.of();
    private List<String> outputAttributes = List.of();
    private List<ValidationType> recordValidationTypes = List.of();

}
