package com.example.processor.application;

import com.example.processor.common.exception.DataFormatException;
import com.example.processor.domain.data.model.DataRecord;
import com.example.processor.domain.data.service.DataExtractService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.example.processor.common.GlobalExceptionHandler.ERR_MSG_CORRUPTED_INPUT_DATA;
import static com.example.processor.common.GlobalExceptionHandler.ERR_MSG_WRONG_FILE_TYPE;

@Service
public class TextFileDataExtractService implements DataExtractService {

    public static final String COL_DELIMITER_REGEX = "\\|";

    public List<DataRecord> extractDataRecords(MultipartFile file, List<String> fields) {
        checkCorrectFileType(file);
        var dataRecords = new ArrayList<DataRecord>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            int index = 0;
            while ((line = br.readLine()) != null && StringUtils.hasText(line)) {
                var parts = line.split(COL_DELIMITER_REGEX);
                if (parts.length != fields.size()) {
                    throw new DataFormatException(ERR_MSG_CORRUPTED_INPUT_DATA);
                }
                dataRecords.add(new DataRecord(index,
                        Arrays.stream(parts).map(String::trim).toList())
                );
                index++;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return dataRecords;
    }

    private void checkCorrectFileType(MultipartFile file) {
        if (!file.getContentType().equals(MediaType.TEXT_PLAIN_VALUE)) {
            throw new DataFormatException(ERR_MSG_WRONG_FILE_TYPE);
        }
    }

}
