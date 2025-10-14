package com.ec.library.config;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditorProvider")
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        // Mặc định là SYSTEM, hoặc override trong service nếu muốn lấy từ SecurityContext
        return Optional.of("SYSTEM");
    }
}