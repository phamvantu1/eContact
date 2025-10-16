package com.ec.customer.service;

import com.ec.customer.common.constant.DefineStatus;
import com.ec.customer.model.DTO.request.RoleRequestDTO;
import com.ec.customer.model.DTO.response.RoleResponseDTO;
import com.ec.customer.model.entity.Permission;
import com.ec.customer.model.entity.Role;
import com.ec.customer.repository.PermissionRepository;
import com.ec.customer.repository.RoleRepository;
import com.ec.library.exception.CustomException;
import com.ec.library.exception.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public Map<String, String> createRole(RoleRequestDTO roleRequestDTO){
        try{
            Set<Permission> permissionList = new HashSet<>();
            if(!roleRequestDTO.getPermissionIds().isEmpty()){
                roleRequestDTO.getPermissionIds().forEach(permissionId -> {
                    Permission permission = permissionRepository.findById(permissionId)
                            .orElseThrow(() -> new CustomException(ResponseCode.PERMISSION_NOT_FOUND));
                    permissionList.add(permission);
                });
            }
            Role role = Role.builder()
                    .name(roleRequestDTO.getName())
                    .status(DefineStatus.ACTIVE.getValue())
                    .permissions(permissionList)
                    .build();

            roleRepository.save(role);

            return Map.of("message", "Tạo mới vai trò thành công");

        }catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Có lỗi trong quá trình tạo mới người dùng : " + e.getMessage());
        }
    }

    public Map<String, String> updateRole(Long roleId, RoleRequestDTO roleRequestDTO){
        try{
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new CustomException(ResponseCode.ROLE_NOT_FOUND));

            Set<Permission> permissionList = new HashSet<>();
            if(!roleRequestDTO.getPermissionIds().isEmpty()){
                roleRequestDTO.getPermissionIds().forEach(permissionId -> {
                    Permission permission = permissionRepository.findById(permissionId)
                            .orElseThrow(() -> new CustomException(ResponseCode.PERMISSION_NOT_FOUND));
                    permissionList.add(permission);
                });
            }

            role.setName(roleRequestDTO.getName());
            role.setPermissions(permissionList);

            roleRepository.save(role);

            return Map.of("message", "Cập nhật vai trò thành công");

        }catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Có lỗi trong quá trình cập nhật vai trò : " + e.getMessage());
        }
    }

    public Map<String, String> deleteRole(Long roleId){
        try{
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new CustomException(ResponseCode.ROLE_NOT_FOUND));

            role.setStatus(DefineStatus.INACTIVE.getValue());

            roleRepository.save(role);

            return Map.of("message", "Xoá vai trò thành công");

        }catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Có lỗi trong quá trình xoá vai trò : " + e.getMessage());
        }
    }

    @Transactional
    public Page<RoleResponseDTO> getAllRoles(int page, int size , String textSearch){
        try{
            Pageable pageable = PageRequest.of(page, size);

            var result = roleRepository.getAllRoles(textSearch, pageable);

            return result.map(this::convertToRoleResponseDTO);

        }catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Có lỗi trong quá trình tìm kiếm vai trò : " + e.getMessage());
        }
    }

    private RoleResponseDTO convertToRoleResponseDTO(Role role) {
        RoleResponseDTO dto = new RoleResponseDTO();
        dto.setId(role.getId());
        dto.setName(role.getName());
        dto.setPermissions(role.getPermissions());
        return dto;
    }

    @Transactional
    public RoleResponseDTO getRoleById(Long roleId){
        try{
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new CustomException(ResponseCode.ROLE_NOT_FOUND));

            return convertToRoleResponseDTO(role);

        }catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Có lỗi trong quá trình tìm kiếm vai trò : " + e.getMessage());
        }
    }
}
