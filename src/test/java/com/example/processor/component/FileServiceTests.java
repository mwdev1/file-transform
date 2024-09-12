package com.example.processor.component;

import com.example.processor.application.FileService;
import com.example.processor.application.TextFileDataExtractService;
import com.example.processor.application.configuration.ServiceConfiguration;
import com.example.processor.domain.data.model.DataRecord;
import com.example.processor.domain.validation.service.ValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.List;

import static com.example.processor.TestBuilder.getFile;
import static org.assertj.core.api.Assertions.assertThat;

public class FileServiceTests {

    FileService fileService;

    @BeforeEach
    public void setServiceConfig() {
        ServiceConfiguration serviceConfiguration = new ServiceConfiguration();
        serviceConfiguration.setInputAttributes(List.of("UUID", "ID", "Name", "Likes", "Transport", "Avg Speed", "Top Speed"));
        serviceConfiguration.setOutputAttributes(List.of("Name", "Transport", "Top Speed"));
        fileService = new FileService(
                new ValidationService(),
                new TextFileDataExtractService(),
                serviceConfiguration,
                new ObjectMapper()
        );
    }

    @Test
    void shouldTransformFileIntoOutputStructureByteArrayResource() throws IOException {
        //arrange
        MockMultipartFile file = getFile("input.txt", MediaType.TEXT_PLAIN);
        //act
        List<DataRecord> output = fileService.transformFile(file);
        //assert
        assertThat(output).hasSize(3);
        assertThat(output).element(0).isEqualTo(new DataRecord(0, List.of("John Smith", "Rides A Bike", "12.1")));
        assertThat(output).element(1).isEqualTo(new DataRecord(1, List.of("Mike Smith", "Drives an SUV", "95.5")));
        assertThat(output).element(2).isEqualTo(new DataRecord(2, List.of("Jenny Walters", "Rides A Scooter", "15.3")));
    }
}
