package com.example.processor.application;

import com.example.processor.application.configuration.ServiceConfiguration;
import com.example.processor.common.exception.DataValidationException;
import com.example.processor.domain.data.model.DataRecord;
import com.example.processor.domain.validation.model.*;
import com.example.processor.domain.validation.service.ValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class FileService {

    public FileService(ValidationService validationService, TextFileDataExtractService textFileDataExtractor, ServiceConfiguration serviceConfiguration, ObjectMapper mapper) {
        this.validationService = validationService;
        this.textFileDataExtractor = textFileDataExtractor;
        this.mapper = mapper;

        inputAttributes = serviceConfiguration.getInputAttributes();
        outputAttributes = serviceConfiguration.getOutputAttributes();
        recordValidationTypes = serviceConfiguration.getRecordValidationTypes();
    }

    @Value("${application.service.validate-content:true}")
    private boolean validationEnabled;

    ValidationService validationService;
    TextFileDataExtractService textFileDataExtractor;
    ObjectMapper mapper;

    private List<String> inputAttributes;
    private List<String> outputAttributes;
    private List<ValidationType> recordValidationTypes;

    public List<DataRecord> transformFile(MultipartFile file) throws IOException {
        List<DataRecord> dataRecords = textFileDataExtractor.extractDataRecords(file, inputAttributes);
        if (validationEnabled) {
            validateData(dataRecords);
        }
        return dataRecords.stream()
                .map(this::toOutputRecord)
                .toList();
    }

    private DataRecord toOutputRecord(DataRecord record) {
        List<String> resultValues = IntStream.range(0, outputAttributes.size())
                .mapToObj(idx -> record.values().get(inputAttributes.indexOf(outputAttributes.get(idx))))
                .toList();
        return new DataRecord(record.recordId(), resultValues);
    }

    private void validateData(List<DataRecord> dataRecords) {
        List<ValidationResult> failedRecords = dataRecords.stream()
                .map(dataRecord -> validationService.validateRecord(dataRecord, inputAttributes, recordValidationTypes))
                .filter(result -> result.isFailure())
                .toList();
        if (failedRecords.size() > 0) {
            throw new DataValidationException("Bad input data found.", failedRecords);
        }
    }

}
