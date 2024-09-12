package com.example.processor.common.exception;

import com.example.processor.domain.validation.model.ValidationResult;
import lombok.Getter;

import java.util.List;

@Getter
public class DataValidationException extends RuntimeException {

   List<ValidationResult> errors;

   public DataValidationException(String message, List<ValidationResult> errors) {
      super(message);
      this.errors = errors;
   }
}
