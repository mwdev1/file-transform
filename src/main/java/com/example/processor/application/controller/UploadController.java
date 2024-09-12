package com.example.processor.application.controller;

import com.example.processor.application.FileService;
import com.example.processor.domain.data.model.DataRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
public class UploadController {

    public static final String OUTCOME_FILENAME = "OutcomeFile.json";
    @Autowired
    private FileService fileService;
    @Autowired
    ObjectMapper mapper;

    @PostMapping(value = "/process", produces = {MediaType.APPLICATION_JSON_VALUE})
    ResponseEntity<ByteArrayResource> process(@RequestPart("file") MultipartFile file) throws IOException {

        List<DataRecord> outputRecords = fileService.transformFile(file);
        ByteArrayResource resource = new ByteArrayResource(mapper.writeValueAsBytes(outputRecords));

        return ResponseEntity.ok()
                .contentLength(resource.contentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + OUTCOME_FILENAME + "\"")
                .body(new ByteArrayResource(mapper.writeValueAsBytes(outputRecords)));
    }

}
