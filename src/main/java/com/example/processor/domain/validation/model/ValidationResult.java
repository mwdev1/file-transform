package com.example.processor.domain.validation.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ValidationResult {

    Status status;
    String message;
    Integer recordId;
    List<String> validationErrors;

    public enum Status {
        SUCCESS, FAILURE
    }



    public static ValidationResult success(String message, Integer recordId) {
        return new ValidationResult(Status.SUCCESS, message, recordId, List.of());
    }

    public static ValidationResult failure(String message, Integer recordId, List<String> errors) {
        return new ValidationResult(Status.FAILURE, message, recordId, errors);
    }

    public Boolean isFailure() {
        return Status.FAILURE == this.status;
    }

}
