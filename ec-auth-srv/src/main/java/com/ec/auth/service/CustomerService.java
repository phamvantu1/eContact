package com.ec.auth.service;

import com.ec.auth.model.entity.Customer;
import com.ec.auth.utils.JwtUtil;
import com.ec.library.constants.ServiceEndpoints;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final RestTemplate restTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    public Customer getCustomerByEmail(String email) {
        try {
            String url = ServiceEndpoints.CUSTOMER_API + "/internal/get-by-email?email=" + email;
            log.info("Gọi Customer API getCustomerByEmail tại URL: {}", url);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            Map<String, Object> body = response.getBody();

            Map<String, Object> data = (Map<String, Object>) body.get("data");
            if (data == null) {
                log.warn("Không có trường 'data' trong phản hồi");
                return null;
            }

            // Nếu có lỗi trong data → trả null (hoặc ném exception tuỳ logic)
            if (data.containsKey("error")) {
                log.warn("Customer API báo lỗi: {}", data.get("error"));
                return null;
            }

            // ✅ Chuyển data thành Customer
            Customer customer = new Customer();
            customer.setId(((Number) data.get("id")).intValue());
            customer.setName((String) data.get("name"));
            customer.setEmail((String) data.get("email"));
            customer.setPassword((String) data.get("password"));
            customer.setPhone((String) data.get("phone"));
            customer.setOrganizationId(((Number) data.get("organizationId")).intValue());
            customer.setBirthday((String) data.get("birthday"));
            customer.setGender((String) data.get("gender"));
            customer.setStatus(String.valueOf(data.get("status")));

            // Xử lý roles
            List<Map<String, Object>> rolesList = (List<Map<String, Object>>) data.get("roles");
            if (rolesList != null) {
                List<String> roleNames = rolesList.stream()
                        .map(role -> (String) role.get("name"))
                        .toList();
                customer.setRoles(roleNames);

                List<String> permissions = rolesList.stream()
                        .flatMap(role -> {
                            List<Map<String, Object>> perms = (List<Map<String, Object>>) role.get("permissions");
                            return perms == null ? Stream.empty() :
                                    perms.stream().map(p -> (String) p.get("name")).filter(Objects::nonNull);
                        })
                        .toList();
                customer.setPermissions(permissions);
            }

            return customer;

        } catch (Exception e) {
            log.error("Lỗi khi gọi Customer API: {}", e.getMessage(), e);
            return null;
        }
    }
}
