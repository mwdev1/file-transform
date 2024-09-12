package com.example.processor.infra;

import com.example.processor.domain.audit.model.RequestOrigin;
import com.example.processor.domain.audit.model.repo.ApiExecutionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class IpValidationClient {

    @Value("${application.clients.ip-validation.url}")
    private String serviceUrl;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ApiExecutionLogRepository requestLogRepository;

    public RequestOrigin getIpInfo(String ip) {
        return restTemplate.getForObject(String.format(serviceUrl, ip), RequestOrigin.class);
    }

}
