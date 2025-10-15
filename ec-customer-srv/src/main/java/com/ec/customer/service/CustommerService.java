package com.ec.customer.service;

import com.ec.customer.common.constant.CustomerStatus;
import com.ec.customer.mapper.CustomerMapper;
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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustommerService {

    private final CustomerRepository customerRepository;
    private final OrganizationRepository organizationRepository;
    private final RoleRepository roleRepository;
    private final CustomerMapper customerMapper;

    public Map<String, String> createCustomer(CustomerRequestDTO customerRequestDTO) {
        try {

            Organization organization = organizationRepository.findById(customerRequestDTO.getOrganizationId())
                    .orElseThrow(() -> new CustomException(ResponseCode.ORGANIZATION_NOT_FOUND));

            Role role = roleRepository.findById(customerRequestDTO.getRoleId())
                    .orElseThrow(() -> new CustomException(ResponseCode.ROLE_NOT_FOUND));

            Customer customer = customerMapper.toEntity(customerRequestDTO);
            customer.setOrganization(organization);
            customer.setRoles(Set.of(role));

            customerRepository.save(customer);

            return Map.of("message", "Tạo mới người dùng thành công");

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Có lỗi trong quá trình tạo mới người dùng : " + e.getMessage());
        }
    }

    public Map<String, String> updateCustomer(Long customerId, CustomerRequestDTO customerRequestDTO) {
        try {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new CustomException(ResponseCode.CUSTOMER_NOT_FOUND));

            Organization organization = organizationRepository.findById(customerRequestDTO.getOrganizationId())
                    .orElseThrow(() -> new CustomException(ResponseCode.ORGANIZATION_NOT_FOUND));

            Role role = roleRepository.findById(customerRequestDTO.getRoleId())
                    .orElseThrow(() -> new CustomException(ResponseCode.ROLE_NOT_FOUND));

            customerMapper.updateEntityFromDto(customerRequestDTO, customer);
            customer.setOrganization(organization);
            customer.setRoles(Set.of(role));

            customerRepository.save(customer);

            return Map.of("message", "Cập nhật người dùng thành công");
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Có lỗi trong quá trình tạo mới người dùng : " + e.getMessage());
        }
    }

    public Map<String,String> deleteCustomer(Long customerId){
        try {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new CustomException(ResponseCode.CUSTOMER_NOT_FOUND));
            customer.setStatus(CustomerStatus.INACTIVE.name());

            customerRepository.save(customer);

            return Map.of("message","Xóa người dùng thành công");
        }catch (CustomException e){
            throw e;
        }catch (Exception e){
            throw new RuntimeException("Có lỗi trong quá trình xóa người dùng : " + e.getMessage());
        }
    }

    public CustomerResponseDTO getCustomerByEmail(String email){
        try{
            Customer customer = customerRepository.findByEmail(email)
                    .orElseThrow(() -> new CustomException(ResponseCode.CUSTOMER_NOT_FOUND));

            return customerMapper.toResponseDTO(customer);

        }catch (CustomException e){
            throw e;
        }catch (Exception e){
            throw new RuntimeException("Có lỗi trong quá trình xóa người dùng : " + e.getMessage());
        }
    }

    @Transactional
    public Map<String, String> registerCustomer(CustomerRequestDTO customerRequestDTO){
        try{

            Customer customer = customerMapper.toEntity(customerRequestDTO);
            customer.setStatus(CustomerStatus.ACTIVE.name());
            customerRepository.save(customer);

            return Map.of("message", "Đăng ký tài khoản thành công");

        }catch (CustomException e){
            throw e;
        }catch (Exception e){
            throw new RuntimeException("Có lỗi trong quá trình đăng ký tài khoản  : " + e.getMessage());
        }
    }

//    public Page<CustomerResponseDTO> getAllCustomer(int page, int size, String textSearch, Long organizationId){
//        try{
//
//            Pageable pageable =  PageRequest.of(page, size);
//
//            List<Customer> customerList = customerRepository.getAllCustomer(textSearch, organizationId, pageable);
//
//        }catch (CustomException e){
//            throw e;
//        }catch (Exception e){
//            throw new RuntimeException("Có lỗi trong quá trình xóa người dùng : " + e.getMessage());
//        }
//    }
}
