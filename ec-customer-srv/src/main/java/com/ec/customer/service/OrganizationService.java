package com.ec.customer.service;

import com.ec.customer.common.constant.DefineStatus;
import com.ec.customer.mapper.OrganizationMapper;
import com.ec.customer.model.DTO.request.OrganizationRequestDTO;
import com.ec.customer.model.DTO.response.OrganizationResponseDTO;
import com.ec.customer.model.entity.Organization;
import com.ec.customer.repository.OrganizationRepository;
import com.ec.library.exception.CustomException;
import com.ec.library.exception.ResponseCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;

    @Transactional
    public Map<String, String> createOrganization(OrganizationRequestDTO organizationRequestDTO){
        try{
            Organization organization = Organization.builder()
                    .name(organizationRequestDTO.getName())
                    .email(organizationRequestDTO.getEmail())
                    .status(DefineStatus.ACTIVE.getValue())
                    .taxCode(organizationRequestDTO.getTaxCode())
                    .build();

            if (organizationRequestDTO.getParentId() != null) {
                Organization parentOrganization = organizationRepository.findById(organizationRequestDTO.getParentId())
                        .orElseThrow(() -> new CustomException(ResponseCode.ORGANIZATION_NOT_FOUND));

                organization.setParent(parentOrganization);
            }

            organizationRepository.save(organization);

            return Map.of("message", "Tạo tổ chức thành công");
        }catch (CustomException e) {
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
    public Map<String, String> updateOrganization(Integer organizationId, OrganizationRequestDTO organizationRequestDTO) {
        try {
            Organization organization = organizationRepository.findById(organizationId)
                    .orElseThrow(() -> new CustomException(ResponseCode.ORGANIZATION_NOT_FOUND));

            organization.setName(organizationRequestDTO.getName());
            organization.setEmail(organizationRequestDTO.getEmail());
            organization.setTaxCode(organizationRequestDTO.getTaxCode());

            if (organizationRequestDTO.getParentId() != null) {
                Organization parentOrganization = organizationRepository.findById(organizationRequestDTO.getParentId())
                        .orElseThrow(() -> new CustomException(ResponseCode.ORGANIZATION_NOT_FOUND));
                organization.setParent(parentOrganization);
            }

            organizationRepository.save(organization);

            return Map.of("message", "Cập nhật tổ chức thành công");
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Có lỗi trong quá trình cập nhật tổ chức: " + e.getMessage());
        }
    }

    @Transactional
    public Page<OrganizationResponseDTO> getAllOrganizations(int page, int size, String textSearch ){
        try{
            Pageable pageable = PageRequest.of(page, size);

            var result = organizationRepository.getAllOrganizations(textSearch, pageable);
            return result.map(organizationMapper::toResponseDTO);
        }catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Có lỗi trong quá trình tìm kiếm : " + e.getMessage());
        }
    }

    @Transactional
    public OrganizationResponseDTO getOrganizationById(Integer organizationId) {
        try {
            Organization organization = organizationRepository.findById(organizationId)
                    .orElseThrow(() -> new CustomException(ResponseCode.ORGANIZATION_NOT_FOUND));

            return organizationMapper.toResponseDTO(organization);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Có lỗi trong quá trình tìm kiếm tổ chức: " + e.getMessage());
        }
    }
}
