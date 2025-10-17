package com.ec.customer.service;

import com.ec.customer.model.entity.Permission;
import com.ec.customer.repository.PermissionRepository;
import com.ec.library.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public Page<Permission> getAllPermissions(int page, int size, String textSearch) {
        try{
            Pageable pageable = PageRequest.of(page, size);
            return permissionRepository.getAllPermission(pageable, textSearch);
        }catch (CustomException e){
            throw e;
        }catch(Exception e){
            throw new RuntimeException("Có lỗi trong quá trình lấy danh sách quyền: " + e.getMessage());
        }
    }
}
