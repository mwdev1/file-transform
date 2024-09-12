package com.example.processor.domain.audit.service;

import com.example.processor.domain.audit.model.ApiExecutionLog;
import com.example.processor.domain.audit.model.RequestOrigin;
import com.example.processor.domain.audit.model.repo.ApiExecutionLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuditService {

    public AuditService(ApiExecutionLogRepository apiExecutionLogRepository) {
        this.apiExecutionLogRepository = apiExecutionLogRepository;
    }

    ApiExecutionLogRepository apiExecutionLogRepository;

    public void logRequest(HttpServletRequest request, RequestOrigin ipInfo, long timeLapsed, int status) {

        String requestUri = request.getRequestURI();
        String requestIp = request.getRemoteAddr(); // TODO handle proxys etc
        String countryCode = ipInfo.countryCode();
        String isp = ipInfo.isp();

        ApiExecutionLog log = ApiExecutionLog.builder()
                .requestIp(requestIp)
                .requestIsp(isp)
                .requestCountryCode(countryCode)
                .requestUri(requestUri)
                .timestamp(Instant.now())
                .responseCode(status)
                .timeLapsed(timeLapsed)
                .build();
        apiExecutionLogRepository.save(log);
    }

}
