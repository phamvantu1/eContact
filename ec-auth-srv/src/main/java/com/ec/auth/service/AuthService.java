package com.ec.auth.service;

import com.ec.auth.model.DTO.request.LoginRequestDTO;
import com.ec.auth.model.DTO.request.CustomerRequestDTO;
import com.ec.auth.model.DTO.response.Response;
import com.ec.auth.model.entity.Customer;
import com.ec.auth.utils.JwtUtil;
import com.ec.library.constants.ServiceEndpoints;
import com.ec.library.exception.CustomException;
import com.ec.library.exception.ResponseCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final RestTemplate restTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private Customer getCustomerByEmail(String email) {
        try {
            String url = ServiceEndpoints.CUSTOMER_API + "/get-by-email?email=" + email;
            log.info("Gọi Customer API tại URL: {}", url);

            ParameterizedTypeReference<Response<Customer>> responseType =
                    new ParameterizedTypeReference<>() {};
            ResponseEntity<Response<Customer>> response = restTemplate.exchange(url, HttpMethod.GET, null, responseType);

            log.info("Phản hồi từ Customer API: {}", response.getBody());
            return response.getBody() != null ? response.getBody().getData() : null;

        } catch (HttpClientErrorException.NotFound e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi gọi Customer API: " + e.getMessage(), e);
        }
    }




    public Map<String, String> login(LoginRequestDTO authRequest) {
        try{
            Customer customer = getCustomerByEmail(authRequest.getEmail());
            if (customer == null) {
                throw new CustomException(ResponseCode.CUSTOMER_NOT_FOUND);
            }

            if (!passwordEncoder.matches(authRequest.getPassword(), customer.getPassword())) {
                throw new CustomException(ResponseCode.PASSWORD_INVALID);
            }

            return Map.of("access_token", jwtUtil.generateToken(customer));
        }catch (CustomException e){
            throw e;
        }catch (Exception e){
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

        } catch (HttpClientErrorException.Conflict e) {
            // Trường hợp trùng email, số điện thoại, v.v.
            throw new CustomException(ResponseCode.CUSTOMER_ALREADY_EXISTS);
        } catch (HttpClientErrorException.BadRequest e) {
            throw new CustomException(ResponseCode.BAD_REQUEST);
        } catch (HttpClientErrorException e) {
            throw new RuntimeException("Lỗi HTTP khi gọi Customer API: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Có lỗi trong quá trình đăng ký tài khoản: " + e.getMessage(), e);
        }
    }



}
