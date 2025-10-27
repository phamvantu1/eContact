package com.ec.library.config;

import com.ec.library.entity.CustomUserDetails;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("auditorProvider")
public class AuditorAwareImpl implements AuditorAware<Integer> {
    @Override
    public Optional<Integer> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            System.out.println("❌ No authentication context");
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomUserDetails userDetails) {
            System.out.println("✅ Current auditor id: " + userDetails.getId());
            return Optional.ofNullable(userDetails.getId());
        }

        System.out.println("⚠️ Principal is not CustomUserDetails: " + principal);
        return Optional.empty();
    }

}
