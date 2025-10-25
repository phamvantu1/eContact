package com.ec.auth.service;

import com.ec.auth.model.DTO.request.LoginRequestDTO;
import com.ec.auth.model.DTO.request.CustomerRequestDTO;
import com.ec.auth.model.DTO.response.Response;
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
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
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
    private final CustomerService customerService;


    public void blacklistToken(String token, long expirationMillis) {
        redisTemplate.opsForValue().set(token, "blacklisted", expirationMillis, TimeUnit.MILLISECONDS);
    }

    // Kiểm tra token có nằm trong danh sách blacklist không
    public boolean isTokenBlacklisted(String token) {
        return redisTemplate.hasKey(token);
    }

    public Map<String, String> login(LoginRequestDTO authRequest) {
        try {
            Customer customer = customerService.getCustomerByEmail(authRequest.getEmail());
            if (customer == null) {
                throw new CustomException(ResponseCode.EMAIL_NOT_FOUND);
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
            String url = ServiceEndpoints.CUSTOMER_API + "/internal/register";
            log.info("Gọi Customer API tại URL: {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            HttpEntity<CustomerRequestDTO> entity = new HttpEntity<>(customerRequestDTO, headers);

            // Gọi API, và nhận JSON trả về dạng Map
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );

            log.info("Phản hồi từ Customer API: {}", response.getBody());
            if (response.getBody() == null || response.getBody().get("data") == null) {
                return Map.of("message", "Phản hồi không hợp lệ từ Customer API");
            }

            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");

            // Nếu có lỗi trong data
            if (data.containsKey("error")) {
                return Map.of("message", String.valueOf(data.get("error")));
            }

            // Nếu thành công
            return Map.of("message", "Đăng ký tài khoản thành công");

        } catch (Exception e) {
            log.error("Lỗi khi gọi Customer API: {}", e.getMessage(), e);
            return Map.of("message", "Lỗi khi gọi Customer API: " + e.getMessage());
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