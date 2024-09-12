package com.example.processor;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;

public class TestBuilder {

    public static MockMultipartFile getFile(String filename, MediaType mediaType) {
        MockMultipartFile file;
        try {
            file = new MockMultipartFile(
                    "file",
                    filename,
                    mediaType.toString(),
                    Files.readAllBytes(new ClassPathResource("data/" + filename).getFile().toPath())
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

}
