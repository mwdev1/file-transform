package com.example.processor.common;

import com.example.processor.domain.audit.model.RequestOrigin;
import com.example.processor.domain.audit.service.AuditService;
import com.example.processor.infra.IpValidationClient;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class RequestSecurityAndAuditFilter extends OncePerRequestFilter {

    @Value("${application.security.blocked-countries}")
    private List<String> blockedCountries;
    @Value("${application.security.blocked-isps}")
    private List<String> blockedIsps;

    @Autowired
    IpValidationClient ipValidationService;
    @Autowired
    AuditService auditService;

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {
        final long start = System.nanoTime();
        RequestOrigin ipInfo = ipValidationService.getIpInfo(request.getRemoteAddr());
        try {
            if (isBlocked(ipInfo)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("The origin of your request is not supported.");
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Request blocked");
            } else {
                filterChain.doFilter(request, response);
            }
        } finally {
            final long end = System.nanoTime();
            log.info("REQUEST INFO: " + ipInfo.toString());
            auditService.logRequest(request, ipInfo,end - start, response.getStatus());
        }
    }

    private boolean isBlocked(RequestOrigin response) {
        return blockedCountries.contains(response.country()) || blockedIsps.contains(response.isp());
    }

}
