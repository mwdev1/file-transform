package com.example.processor.domain.audit.model.repo;

import com.example.processor.domain.audit.model.ApiExecutionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApiExecutionLogRepository extends JpaRepository<ApiExecutionLog, UUID> {
    List<ApiExecutionLog> findAllByRequestIp(String ip);
}
