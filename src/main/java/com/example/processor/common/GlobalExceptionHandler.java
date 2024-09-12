package com.example.processor.common;

import com.example.processor.common.exception.DataFormatException;
import com.example.processor.common.exception.DataValidationException;
import com.example.processor.domain.validation.model.ValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalExceptionHandler {
   public static final String ERR_MSG_CORRUPTED_INPUT_DATA = "Corrupted input data found. Make sure the file is in the expected format and structure.";
   public static final String ERR_MSG_WRONG_FILE_TYPE = "Invalid file type. Please the uploaded file is in the correct format.";

   @ResponseStatus(BAD_REQUEST)
   @ExceptionHandler(DataValidationException.class)
   public List<ValidationResult> onDataValidationException(DataValidationException ex) {
      log.error("Validation failed. Failed records: " + ex.getErrors().size());
      return ex.getErrors();
   }

   @ResponseStatus(BAD_REQUEST)
   @ExceptionHandler(DataFormatException.class)
   public String onDataFormatException(DataFormatException ex) {
      log.error(ex.getMessage());
      return ex.getMessage(); // TODO standardise json error response
   }

   // TODO handle and standardise others

}