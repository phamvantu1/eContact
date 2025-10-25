package com.ec.customer.service;

import com.ec.customer.common.constant.CustomerStatus;
import com.ec.customer.common.constant.DefineStatus;
import com.ec.customer.mapper.CustomerMapper;
import com.ec.customer.model.DTO.request.ChangePasswordDTO;
import com.ec.customer.model.DTO.request.CustomerRequestDTO;
import com.ec.customer.model.DTO.response.CustomerResponseDTO;
import com.ec.customer.model.entity.Customer;
import com.ec.customer.model.entity.Organization;
import com.ec.customer.model.entity.Role;
import com.ec.customer.repository.CustomerRepository;
import com.ec.customer.repository.OrganizationRepository;
import com.ec.customer.repository.RoleRepository;
import com.ec.library.exception.CustomException;
import com.ec.library.exception.ResponseCode;
import com.ec.library.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final CustomerMapper customerMapper;
    private final PasswordEncoder passwordEncoder;

    private static final String passwordDefault = "123456";
    private static final String roleUser = "USER";
    private static final Integer defaultOrganizationId = 1;

    @Transactional
    public Map<String, String> createCustomer(CustomerRequestDTO customerRequestDTO) {
        try {

            Customer oldCustomer = customerRepository.findByEmail(customerRequestDTO.getEmail()).orElse(null);
            if(oldCustomer != null){
                throw new CustomException(ResponseCode.CUSTOMER_EMAIL_EXISTED);
            }

            Organization organization = organizationRepository.findById(customerRequestDTO.getOrganizationId())
                    .orElseThrow(() -> new CustomException(ResponseCode.ORGANIZATION_NOT_FOUND));

            Role role = roleRepository.findById(customerRequestDTO.getRoleId())
                    .orElseThrow(() -> new CustomException(ResponseCode.ROLE_NOT_FOUND));

            Customer customer = customerMapper.toEntity(customerRequestDTO);
            customer.setOrganization(organization);
            customer.setRoles(Set.of(role));

            customer.setPassword(passwordEncoder.encode(passwordDefault));

            customerRepository.save(customer);

            return Map.of("message", "Tạo mới người dùng thành công");

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Có lỗi trong quá trình tạo mới người dùng : " + e.getMessage());
        }
    }

    @Transactional
    public Map<String, String> updateCustomer(Integer customerId, CustomerRequestDTO customerRequestDTO) {
        try {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new CustomException(ResponseCode.CUSTOMER_NOT_FOUND));

            Organization organization = organizationRepository.findById(customerRequestDTO.getOrganizationId())
                    .orElseThrow(() -> new CustomException(ResponseCode.ORGANIZATION_NOT_FOUND));

            Role role = roleRepository.findById(customerRequestDTO.getRoleId())
                    .orElseThrow(() -> new CustomException(ResponseCode.ROLE_NOT_FOUND));

            customerMapper.updateEntityFromDto(customerRequestDTO, customer);
            customer.setOrganization(organization);
            customer.setRoles(new HashSet<>(Collections.singletonList(role)));

            customerRepository.save(customer);

            return Map.of("message", "Cập nhật người dùng thành công");
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Có lỗi trong quá trình tạo mới người dùng : " + e.getMessage());
        }
    }

    @Transactional
    public Map<String,String> deleteCustomer(Integer customerId){
        try {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new CustomException(ResponseCode.CUSTOMER_NOT_FOUND));
            customer.setStatus(DefineStatus.INACTIVE.getValue());

            customerRepository.save(customer);

            return Map.of("message","Xóa người dùng thành công");
        }catch (CustomException e){
            throw e;
        }catch (Exception e){
            throw new RuntimeException("Có lỗi trong quá trình xóa người dùng : " + e.getMessage());
        }
    }

    @Transactional
    public Page<CustomerResponseDTO> getAllCustomer(int page, int size, String textSearch, Integer organizationId){
        try{

            Pageable pageable =  PageRequest.of(page, size);

            Page<Customer> customerList = customerRepository.getAllCustomer(textSearch, organizationId, pageable);

            return customerList.map(customerMapper::toResponseDTO);

        }catch (CustomException e){
            throw e;
        }catch (Exception e){
            throw new RuntimeException("Có lỗi trong quá trình xóa người dùng : " + e.getMessage());
        }
    }

    @Transactional
    public CustomerResponseDTO getCustomerById(Integer customerId){
        try{
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new CustomException(ResponseCode.CUSTOMER_NOT_FOUND));

            return customerMapper.toResponseDTO(customer);

        }catch (CustomException e){
            throw e;
        }catch (Exception e){
            throw new RuntimeException("Có lỗi trong quá trình xóa người dùng : " + e.getMessage());
        }
    }

    public Response<?> getCustomerByEmail(String email){
        try {
            log.info("Email nhận được: {}", email);
            Customer customer = customerRepository.findByEmail(email).orElse(null);

            if (customer == null) {
                return Response.success(Map.of("error", "Không tìm thấy người dùng với email: " + email));
            }

            return Response.success(customerMapper.toResponseDTO(customer));

        }catch (Exception e){
            throw new RuntimeException("Có lỗi trong quá trình xóa người dùng : " + e.getMessage());
        }
    }

    @Transactional
    public Response<?> registerCustomer(CustomerRequestDTO customerRequestDTO){
        try{
            log.info("Đăng ký khách hàng với email: {}", customerRequestDTO.getEmail());
            Customer oldCustomer = customerRepository.findByEmail(customerRequestDTO.getEmail()).orElse(null);
            if(oldCustomer != null){
              return Response.success(Map.of("error", "Email đã tồn tại trong hệ thống"));
            }

            Role role = roleRepository.findByName(roleUser).orElse(null);

            if(role == null) return Response.success(Map.of("error", "Vai trò người dùng không tồn tại trong hệ thống"));

            Organization organization = organizationRepository.findById(defaultOrganizationId).orElse(null);
            if(organization == null) return Response.success(Map.of("error", "Tổ chức mặc định không tồn tại trong hệ thống"));
            Customer customer = customerMapper.toEntity(customerRequestDTO);

            customer.setStatus(DefineStatus.ACTIVE.getValue());
            customer.setRoles(Set.of(role));
            customer.setOrganization(organization);
            customer.setPassword(passwordEncoder.encode(customerRequestDTO.getPassword()));

            customerRepository.save(customer);

            return Response.success(Map.of("message", "Đăng ký tài khoản thành công"));

        }catch (Exception e){
            throw new RuntimeException("Có lỗi trong quá trình đăng ký tài khoản  : " + e.getMessage());
        }
    }

    @Transactional
    public Map<String, String> changePassword(Integer customerId, ChangePasswordDTO changePasswordDTO){
        try{
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new CustomException(ResponseCode.CUSTOMER_NOT_FOUND));

            String oldPassword = changePasswordDTO.getOldPassword();
            String newPassword = changePasswordDTO.getNewPassword();
            String confirmNewPassword = changePasswordDTO.getConfirmPassword();

            if(!passwordEncoder.matches(oldPassword, customer.getPassword())){
                throw new CustomException(ResponseCode.INVALID_OLD_PASSWORD);
            }

            if(!newPassword.equals(confirmNewPassword)){
                throw new CustomException(ResponseCode.CONFIRM_PASSWORD_NOT_MATCH);
            }

            customer.setPassword(passwordEncoder.encode(newPassword));
            customerRepository.save(customer);

            return Map.of("message", "Đổi mật khẩu thành công");

        }catch (CustomException e){
            throw e;
        }catch (Exception e){
            throw new RuntimeException("Có lỗi trong quá trình đổi mật khẩu  : " + e.getMessage());
        }
    }
}

