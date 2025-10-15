package com.ec.auth.service;

import com.ec.auth.model.DTO.request.LoginRequestDTO;
import com.ec.auth.model.DTO.request.CustomerRequestDTO;
import com.ec.auth.model.entity.Customer;
import com.ec.auth.utils.JwtUtil;
import com.ec.library.constants.ServiceEndpoints;
import com.ec.library.exception.CustomException;
import com.ec.library.exception.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final RestTemplate restTemplate;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private Customer getCustomerByEmail(String email) {
        String url = ServiceEndpoints.CUSTOMER_API + "/by-email?email=" + email;
        return restTemplate.getForObject(url, Customer.class);
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

    public Map<String, String> register(CustomerRequestDTO customerRequestDTO){
        try{

            return Map.of("message", "Đăng ký tài khoản thành công");

        }catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Có lỗi trong quá trình đăng ký tài khoản  : " + e.getMessage());
        }
    }


}
