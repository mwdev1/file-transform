package com.example.processor.domain.data.service;

import com.example.processor.domain.data.model.DataRecord;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface DataExtractService {

    List<DataRecord> extractDataRecords(MultipartFile file, List<String> attrs);

}
