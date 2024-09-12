package com.example.processor.domain.validation.service;

import com.example.processor.domain.data.model.DataRecord;
import com.example.processor.domain.validation.model.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
public class ValidationService {

    private static final String IS_VALID = "IS_VALID";
    private static final String IDENTIFIER_REGEX = "^[A-Z0-9]+$";
    private static final String POSITIVE_NUMBER_REGEX = "[+-]?\\d+\\.?\\d*";
    private static final String NAME_REGEX = "^[a-zA-ZÀ-ÖØ-öø-ÿ' -]+$";
    public static final String MSG_INVALID_UUID_FORMAT = "Invalid UUID format.";
    public static final String MSG_IVALID_NAME = "Special characters are not allowed.";
    public static final String MSG_INVALID_NUMBER = "Decimal value required.";
    public static final String MSG_INVALID_IDENTIFIER = "Only capital letters and numbers allowed.";

    public ValidationResult validateRecord(DataRecord dataRecord, List<String> names, List<ValidationType> validationTypes) {
        checkInputDataIntegrity(dataRecord, names, validationTypes);
        List<String> values = dataRecord.values();
        List<String> validationErrors = IntStream.range(0, values.size())
                .mapToObj(index -> names.get(index) + ": " + validateValue(values.get(index), validationTypes.get(index)))
                .filter(msg -> ! msg.contains(IS_VALID))
                .toList();
        if (validationErrors.isEmpty()) {
            return ValidationResult.success("No errors found", dataRecord.recordId());
        }
        return ValidationResult.failure("Please correct the validation errors.", dataRecord.recordId(), validationErrors);
    }

    private void checkInputDataIntegrity(DataRecord dataRecord, List<String> names, List<ValidationType> validationTypes) {
        if (dataRecord.values().size() != names.size() || names.size() != validationTypes.size()) {
            throw new IllegalArgumentException("Name list and validation types must match DataRecord value list.");
        }
    }

    private String validateValue(String value, ValidationType validationType) {
        switch (validationType) {
            case IDENTIFIER:
                return validatePattern(value, IDENTIFIER_REGEX, MSG_INVALID_IDENTIFIER);

            case POSITIVE_NUMBER:
                return validatePattern(value, POSITIVE_NUMBER_REGEX, MSG_INVALID_NUMBER);

            case UUID:
                return validateUUID(value);

            case NAME:
                return validatePattern(value, NAME_REGEX, MSG_IVALID_NAME);

            default:
                return IS_VALID;
        }
    }

    private String validatePattern(String value, String regex, String errorMessage) {
        return value.matches(regex) ? IS_VALID : errorMessage;
    }

    private String validateUUID(String value) {
        try {
            UUID.fromString(value);
            return IS_VALID;
        } catch (IllegalArgumentException e) {
            return MSG_INVALID_UUID_FORMAT;
        }
    }

}
