package com.ec.customer.service;

import com.ec.customer.common.constant.DefineStatus;
import com.ec.customer.mapper.OrganizationMapper;
import com.ec.customer.model.DTO.request.OrganizationRequestDTO;
import com.ec.customer.model.DTO.response.OrganizationResponseDTO;
import com.ec.customer.model.entity.Customer;
import com.ec.customer.model.entity.Organization;
import com.ec.customer.repository.CustomerRepository;
import com.ec.customer.repository.OrganizationRepository;
import com.ec.library.exception.CustomException;
import com.ec.library.exception.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;
    private final CustomerService customerService;
    private final CustomerRepository customerRepository;

    @Transactional
    public OrganizationResponseDTO createOrganization(OrganizationRequestDTO organizationRequestDTO) {
        try {
            Organization organization = Organization.builder()
                    .name(organizationRequestDTO.getName())
                    .email(organizationRequestDTO.getEmail())
                    .status(DefineStatus.ACTIVE.getValue())
                    .code(organizationRequestDTO.getCode())
                    .parentId(organizationRequestDTO.getParentId())
                    .taxCode(organizationRequestDTO.getTaxCode())
                    .build();

            organizationRepository.save(organization);

            return organizationMapper.toDTO(organization);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Có lỗi trong quá trình tạo tổ chức : " + e.getMessage());
        }
    }

    @Transactional
    public Map<String, String> deleteOrganization(Integer organizationId) {
        try {
            Organization organization = organizationRepository.findById(organizationId)
                    .orElseThrow(() -> new CustomException(ResponseCode.ORGANIZATION_NOT_FOUND));

            organization.setStatus(DefineStatus.INACTIVE.getValue());
            organizationRepository.save(organization);

            return Map.of("message", "Xoá tổ chức thành công");
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Có lỗi trong quá trình xoá tổ chức: " + e.getMessage());
        }
    }

    @Transactional
    public OrganizationResponseDTO updateOrganization(Integer organizationId, OrganizationRequestDTO organizationRequestDTO) {
        try {
            Organization organization = organizationRepository.findById(organizationId)
                    .orElseThrow(() -> new CustomException(ResponseCode.ORGANIZATION_NOT_FOUND));

            organization.setName(organizationRequestDTO.getName());
            organization.setEmail(organizationRequestDTO.getEmail());
            organization.setTaxCode(organizationRequestDTO.getTaxCode());
            organization.setCode(organizationRequestDTO.getCode());
            organization.setParentId(organizationRequestDTO.getParentId());

            organizationRepository.save(organization);

            return organizationMapper.toDTO(organization);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Có lỗi trong quá trình cập nhật tổ chức: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Page<OrganizationResponseDTO> getAllOrganizations(int page, int size, String textSearch) {
        try {
            Pageable pageable = PageRequest.of(page, size);

            // Gọi repository
            Page<Organization> organizationPage = organizationRepository.getAllOrganizations(textSearch, pageable);

            // Map sang DTO
            List<OrganizationResponseDTO> dtoList = organizationMapper.toDTOList(organizationPage.getContent());

            // Trả về Page<DTO>
            return new PageImpl<>(dtoList, pageable, organizationPage.getTotalElements());

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Có lỗi trong quá trình tìm kiếm: " + e.getMessage());
        }
    }


    @Transactional
    public OrganizationResponseDTO getOrganizationById(Integer organizationId) {
        try {
            Organization organization = organizationRepository.findByIdAndStatus(organizationId, DefineStatus.ACTIVE.getValue())
                    .orElseThrow(() -> new CustomException(ResponseCode.ORGANIZATION_NOT_FOUND));

            return organizationMapper.toDTO(organization);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Có lỗi trong quá trình tìm kiếm tổ chức: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public OrganizationResponseDTO getOrganizationByCustomerEmail(String customerEmail) {
        try {

            log.info("Tìm tổ chức cho khách hàng với mail: {}", customerEmail);

            Customer customer = customerRepository.findByEmail(customerEmail).orElse(null);

            if (customer == null) return null;

            Organization organization = organizationRepository.findById(customer.getOrganization().getId()).orElse(null);

            if (organization == null) return null;

            return organizationMapper.toDTO(organization);

        } catch (Exception e) {
            throw new RuntimeException("Có lỗi trong quá trình tìm kiếm tổ chức theo khách hàng: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public OrganizationResponseDTO getOrganizationByIdInternal(Integer organizationId) {
        try {

            log.info("Tìm tổ chức cho id: {}", organizationId);

            Customer customer = customerRepository.findById(organizationId).orElse(null);

            if (customer == null) return null;

            Organization organization = organizationRepository.findById(customer.getOrganization().getId()).orElse(null);

            if (organization == null) return null;

            return organizationMapper.toDTO(organization);

        } catch (Exception e) {
            throw new RuntimeException("Có lỗi trong quá trình tìm kiếm tổ chức theo id : " + e.getMessage());
        }
    }
}
