package com.ec.auth.service;

import com.ec.auth.model.DTO.request.LoginRequestDTO;
import com.ec.auth.model.DTO.request.CustomerRequestDTO;
import com.ec.auth.model.entity.Customer;
import com.ec.auth.utils.JwtUtil;
import com.ec.library.constants.ServiceEndpoints;
import com.ec.library.exception.CustomException;
import com.ec.library.exception.CustomerApiException;
import com.ec.library.exception.ResponseCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final RestTemplate restTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, Object> redisTemplate;


    public void blacklistToken(String token, long expirationMillis) {
        redisTemplate.opsForValue().set(token, "blacklisted", expirationMillis, TimeUnit.MILLISECONDS);
    }

    // Kiểm tra token có nằm trong danh sách blacklist không
    public boolean isTokenBlacklisted(String token) {
        return redisTemplate.hasKey(token);
    }

    public Customer getCustomerByEmail(String email) {
        try {
            String url = ServiceEndpoints.CUSTOMER_API + "/get-by-email?email=" + email;
            log.info("Gọi Customer API tại URL: {}", url);

            // Gọi API
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {
                    }
            );

            log.info("Phản hồi từ Customer API: {}", response.getBody());

            if (response.getBody() == null || response.getBody().get("data") == null) {
                return null;
            }

            // Lấy object "data" ra
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> dataMap = (Map<String, Object>) response.getBody().get("data");

            // Chuyển thành Customer entity của Auth Service
            Customer customer = new Customer();
            customer.setId(((Number) dataMap.get("id")).intValue());
            customer.setName((String) dataMap.get("name"));
            customer.setEmail((String) dataMap.get("email"));
            customer.setPassword((String) dataMap.get("password"));
            customer.setPhone((String) dataMap.get("phone"));
            customer.setBirthday((String) dataMap.get("birthday"));
            customer.setGender((String) dataMap.get("gender"));
            customer.setStatus(String.valueOf(dataMap.get("status")));

            // Xử lý roles
            List<Map<String, Object>> rolesList = (List<Map<String, Object>>) dataMap.get("roles");
            if (rolesList != null) {
                List<String> roleNames = rolesList.stream()
                        .map(role -> (String) role.get("name"))
                        .toList();
                customer.setRoles(roleNames);

                // Gom toàn bộ permissions (nếu có)
                List<String> allPermissions = rolesList.stream()
                        .flatMap(role -> {
                            List<Map<String, Object>> perms = (List<Map<String, Object>>) role.get("permissions");
                            if (perms == null) return Stream.empty();
                            return perms.stream()
                                    .map(p -> (String) p.get("name"))
                                    .filter(Objects::nonNull);
                        })
                        .toList();
                customer.setPermissions(allPermissions);
            }

            return customer;

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            // Ném lên CustomerApiException để GlobalExceptionHandler xử lý
            throw new CustomerApiException(ex.getStatusCode().value(), ex.getResponseBodyAsString());
        }catch (Exception e) {
            log.error("Lỗi khi gọi Customer API: {}", e.getMessage(), e);
            throw new RuntimeException("Lỗi khi gọi Customer API: " + e.getMessage(), e);
        }
    }


    public Map<String, String> login(LoginRequestDTO authRequest) {
        try {
            Customer customer = getCustomerByEmail(authRequest.getEmail());
            if (customer == null) {
                throw new CustomException(ResponseCode.EMAIL_INVALID);
            }

            if (!passwordEncoder.matches(authRequest.getPassword(), customer.getPassword())) {
                throw new CustomException(ResponseCode.PASSWORD_INVALID);
            }

            return Map.of("access_token", jwtUtil.generateToken(customer));
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Đăng nhập thất bại : " + e.getMessage());
        }
    }

    public Map<String, String> register(CustomerRequestDTO customerRequestDTO) {
        try {
            ResponseEntity<Void> response = restTemplate.postForEntity(
                    ServiceEndpoints.CUSTOMER_API + "/register",
                    customerRequestDTO,
                    Void.class
            );

            // ✅ Kiểm tra HTTP status
            if (response.getStatusCode().is2xxSuccessful()) {
                return Map.of("message", "Đăng ký tài khoản thành công");
            } else {
                throw new RuntimeException("Đăng ký thất bại với mã lỗi: " + response.getStatusCode());
            }

        }catch (HttpClientErrorException | HttpServerErrorException ex) {
            // Ném lên CustomerApiException để GlobalExceptionHandler xử lý
            throw new CustomerApiException(ex.getStatusCode().value(), ex.getResponseBodyAsString());
        } catch (Exception ex) {
            throw new RuntimeException("Có lỗi trong quá trình đăng ký tài khoản: " + ex.getMessage(), ex);
        }
    }

    public Map<String, String> logout(HttpServletRequest request){
        try{

            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                // Giả sử bạn lấy expiration còn lại từ JWT
                long remainingTime = 86400000; // ví dụ 1 ngày, bạn có thể parse từ JWT

                blacklistToken(token, remainingTime);
                return Map.of("message", "Đăng xuất thành công");
            }
            throw new CustomException(ResponseCode.UNAUTHORIZED);

        }catch (CustomException e) {
            throw e;
        }
        catch (Exception e) {
            throw new RuntimeException("Logout failed: " + e.getMessage());
        }
    }

}