package com.example.processor.component;

import com.example.processor.domain.data.model.DataRecord;
import com.example.processor.domain.validation.model.ValidationResult;
import com.example.processor.domain.validation.model.ValidationType;
import com.example.processor.domain.validation.service.ValidationService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.example.processor.domain.validation.model.ValidationResult.Status.FAILURE;
import static com.example.processor.domain.validation.model.ValidationResult.Status.SUCCESS;
import static com.example.processor.domain.validation.model.ValidationType.*;
import static com.example.processor.domain.validation.service.ValidationService.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ValidationServiceTests {

    ValidationService validationService = new ValidationService();

    @Test
    void shouldReturnSuccessValidationStatusForValidRecord() {
        DataRecord dataRecord = new DataRecord(0, List.of("100", java.util.UUID.randomUUID().toString(), "ABCD", "John Doe", "###"));
        List<String> names = List.of("col1", "col2", "col3", "col4", "col5");
        List<ValidationType> validationTypes = List.of(POSITIVE_NUMBER, UUID, IDENTIFIER, NAME, NONE);

        ValidationResult validationResult = validationService.validateRecord(dataRecord, names, validationTypes);

        assertThat(validationResult.getStatus()).isEqualTo(SUCCESS);
        assertThat(validationResult.getValidationErrors()).hasSize(0);
    }

    @Test
    void shouldReturnFailureValidationStatusForInvalidRecord() {
        DataRecord dataRecord = new DataRecord(0, List.of("###", "###", "###", "###", "###"));
        List<String> names = List.of("col1", "col2", "col3", "col4", "col5");
        List<ValidationType> validationTypes = List.of(POSITIVE_NUMBER, UUID, IDENTIFIER, NAME, NONE);

        ValidationResult validationResult = validationService.validateRecord(dataRecord, names, validationTypes);

        assertThat(validationResult.getStatus()).isEqualTo(FAILURE);
        assertThat(validationResult.getValidationErrors()).hasSize(4);
        assertThat(validationResult.getValidationErrors().get(0)).contains(MSG_INVALID_NUMBER);
        assertThat(validationResult.getValidationErrors().get(1)).contains(MSG_INVALID_UUID_FORMAT);
        assertThat(validationResult.getValidationErrors().get(2)).contains(MSG_INVALID_IDENTIFIER);
        assertThat(validationResult.getValidationErrors().get(3)).contains(MSG_IVALID_NAME);
    }

    @Test
    void shouldThrowInvalidArgumentExceptionWhenInconsistentInputData() {
        DataRecord dataRecord = new DataRecord(0, List.of("val1", "val2"));
        List<String> names = List.of("col1", "col2", "col3");
        List<ValidationType> validationTypes = List.of(NONE, NONE, NONE, NONE, NONE);

        assertThrows(IllegalArgumentException.class,
                () -> validationService.validateRecord(dataRecord, names, validationTypes));
    }

}
