package com.example.processor.integration;

import com.example.processor.application.configuration.ServiceConfiguration;
import com.example.processor.domain.audit.model.ApiExecutionLog;
import com.example.processor.domain.audit.model.repo.ApiExecutionLogRepository;
import com.example.processor.domain.validation.model.ValidationResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.processor.TestBuilder.getFile;
import static com.example.processor.application.controller.UploadController.OUTCOME_FILENAME;
import static com.example.processor.common.GlobalExceptionHandler.ERR_MSG_CORRUPTED_INPUT_DATA;
import static com.example.processor.common.GlobalExceptionHandler.ERR_MSG_WRONG_FILE_TYPE;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles({"wiremock", "db-mem"})
@AutoConfigureWireMock(port = 0) // random port

@AutoConfigureMockMvc
@Transactional
public class DataApplicationTests {

  public static final String REMOTE_IP = "127.0.0.1";
  @Autowired
  MockMvc mockMvc;
  @Autowired
  ApiExecutionLogRepository repo;
  @Autowired
  WireMockServer wireMock;
  @Autowired
  ObjectMapper objectMapper;
  @Autowired
  ServiceConfiguration serviceConfiguration;

  @Test
  void fileProcessedSuccessfully() throws Exception {

    stubWiremockIpValidationClientResponse("""
                            {
                              "status": "success",
                              "country": "Canada",
                              "countryCode": "CA",
                              "city": "Montreal",
                              "isp": "Le Groupe Videotron Ltee"
                            }
    """);

    uploadFileApi(getFile("input.txt", MediaType.TEXT_PLAIN))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(header().string("Content-Disposition", "attachment; filename=\"" + OUTCOME_FILENAME + "\""))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].values[0]").value("John Smith"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].values[1]").value("Rides A Bike"))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].values[2]").value("12.1"));

    List<ApiExecutionLog> logs = repo.findAllByRequestIp(REMOTE_IP);
    assertThat(logs).hasSize(1)
            .element(0)
            .hasFieldOrPropertyWithValue("requestIp", REMOTE_IP)
            .hasFieldOrPropertyWithValue("requestIsp", "Le Groupe Videotron Ltee")
            .hasFieldOrPropertyWithValue("requestCountryCode", "CA")
            .hasFieldOrPropertyWithValue("responseCode", HttpStatus.OK.value());
  }

  @Test
  void fileProcessedWithValidationErrorResponse() throws Exception {

    stubWiremockIpValidationClientResponse("""
                            {"status": "success","country": "Canada","countryCode": "CA","city": "Montreal","isp": "dummy"}
    """);

    MvcResult mvcResult = uploadFileApi(getFile("input-invalid.txt", MediaType.TEXT_PLAIN))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andReturn();
    List<ValidationResult> responseObject = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), new TypeReference<>() {});

    List<ApiExecutionLog> logs = repo.findAllByRequestIp(REMOTE_IP);
    assertThat(responseObject).allMatch(ValidationResult::isFailure, "Only the failed records are returned.");

    List<String> errors1 = responseObject.get(0).getValidationErrors();
    assertThat(errors1.get(0)).isEqualTo("UUID: Invalid UUID format.");
    assertThat(errors1.get(1)).isEqualTo("ID: Only capital letters and numbers allowed.");
    assertThat(errors1.get(2)).isEqualTo("Name: Special characters are not allowed.");
    assertThat(errors1.get(3)).isEqualTo("Avg Speed: Decimal value required.");
    assertThat(errors1.get(4)).isEqualTo("Top Speed: Decimal value required.");
  }

  @Test
  void corruptedFileRejectedWithBadRequestResponse() throws Exception {

    stubWiremockIpValidationClientResponse("""
                            {"status": "success","country": "Canada","countryCode": "CA","city": "Montreal","isp": "dummy"}
    """);

    uploadFileApi(getFile("input-corrupted.txt", MediaType.TEXT_PLAIN))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string(ERR_MSG_CORRUPTED_INPUT_DATA))
            .andReturn();
  }

  @Test
  void wrongFileTypeRejectedWithBadRequestResponse() throws Exception {

    stubWiremockIpValidationClientResponse("""
                            {"status": "success","country": "Canada","countryCode": "CA","city": "Montreal","isp": "dummy"}
    """);

    uploadFileApi(getFile("blank.pdf", MediaType.APPLICATION_PDF))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string(ERR_MSG_WRONG_FILE_TYPE))
            .andReturn();
  }

  @Test
  void fileProcessingRequestedAccessDenied() throws Exception {

    stubWiremockIpValidationClientResponse("""
                            {
                              "status": "success",
                              "country": "USA",
                              "countryCode": "US",
                              "city": "Washington",
                              "isp": "Google Cloud"
                            }
    """);

    uploadFileApi(getFile("input.txt", MediaType.TEXT_PLAIN))
            .andExpect(status().isForbidden())
            .andExpect(MockMvcResultMatchers.content().string("The origin of your request is not supported."));

    List<ApiExecutionLog> logs = repo.findAllByRequestIp(REMOTE_IP);
    assertThat(logs).hasSize(1)
            .element(0)
            .hasFieldOrPropertyWithValue("requestIp", REMOTE_IP)
            .hasFieldOrPropertyWithValue("requestIsp", "Google Cloud")
            .hasFieldOrPropertyWithValue("requestCountryCode", "US")
            .hasFieldOrPropertyWithValue("responseCode", 403);
  }

  private void stubWiremockIpValidationClientResponse(String jsonPayload) {
    wireMock.stubFor(get(urlEqualTo("/json/" + REMOTE_IP))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(jsonPayload)));
  }

  private ResultActions uploadFileApi(MockMultipartFile file) throws Exception {
    return mockMvc.perform(MockMvcRequestBuilders
            .multipart("/api/v1/files/process")
            .file(file)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .accept(MediaType.APPLICATION_JSON_VALUE)
    );
  }

}
