package com.example.processor.common.exception;

import lombok.Getter;

@Getter
public class DataFormatException extends RuntimeException {
    public DataFormatException(String message) {
        super(message);
    }
}