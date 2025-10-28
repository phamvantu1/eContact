package com.ec.contract.service;

import com.ec.contract.constant.BaseStatus;
import com.ec.contract.mapper.TypeMapper;
import com.ec.contract.model.dto.TypeDTO;
import com.ec.contract.model.entity.Type;
import com.ec.contract.repository.TypeRepository;
import com.ec.library.exception.CustomException;
import com.ec.library.exception.ResponseCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class TypeService {

    private final TypeRepository typeRepository;
    private final TypeMapper typeMapper;

    public TypeDTO createType(TypeDTO typeDTO) {
        try {
            Type type = typeMapper.toEntity(typeDTO);

            type.setStatus(BaseStatus.ACTIVE.ordinal());

            var result = typeRepository.save(type);

            return typeMapper.toDTO(result);
        } catch (Exception e) {
            log.error("Error creating type: {}", e.getMessage());
            throw e;
        }
    }

    public TypeDTO updateType(Integer typeId, TypeDTO typeDTO) {
        try {

            Type type = typeRepository.findById(typeId).orElseThrow(() -> new CustomException(ResponseCode.TYPE_NOT_FOUND));
            type.setName(typeDTO.getName());
            type.setOrganizationId(typeDTO.getOrganizationId());

            var result = typeRepository.save(type);

            return typeMapper.toDTO(result);
        } catch (CustomException ce) {
            log.error("Custom error updating type: {}", ce.getMessage());
            throw ce;
        } catch (Exception e) {
            log.error("Error updating type: {}", e.getMessage());
            throw e;
        }
    }

    public Map<String, String> deleteType(Integer typeId) {
        try {
            Type type = typeRepository.findById(typeId).orElseThrow(() -> new CustomException(ResponseCode.TYPE_NOT_FOUND));

            type.setStatus(BaseStatus.IN_ACTIVE.ordinal());

            typeRepository.save(type);

            return Map.of("message", "Type deleted successfully");
        } catch (CustomException ce) {
            log.error("Custom error deleting type: {}", ce.getMessage());
            throw ce;
        } catch (Exception e) {
            log.error("Error deleting type: {}", e.getMessage());
            throw e;
        }
    }

    public Page<TypeDTO> getAllTypes(Integer page, Integer size, String textSearch,Integer organizationId) {
        try {
            Pageable pageable = PageRequest.of(page, size);

            Page<Type> typesPage = typeRepository.findByNameContainingAndStatus(textSearch,organizationId, pageable);

            return typesPage.map(typeMapper::toDTO);
        } catch (Exception e) {
            log.error("Error retrieving types: {}", e.getMessage());
            throw e;
        }
    }

    public TypeDTO getTypeById(Integer id) {
        try {
            Type type = typeRepository.findById(id).orElseThrow(() -> new CustomException(ResponseCode.TYPE_NOT_FOUND));

            return typeMapper.toDTO(type);
        } catch (CustomException ce) {
            log.error("Custom error retrieving type by ID: {}", ce.getMessage());
            throw ce;
        } catch (Exception e) {
            log.error("Error retrieving type by ID: {}", e.getMessage());
            throw e;
        }
    }


}
