package com.example.processor.domain.audit.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiExecutionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String requestUri;
    private Instant timestamp;
    private String requestIp;
    private String requestCountryCode;
    private String requestIsp;
    private long timeLapsed;
    private int responseCode;

}
