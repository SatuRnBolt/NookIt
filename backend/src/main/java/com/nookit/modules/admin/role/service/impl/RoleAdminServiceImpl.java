package com.nookit.modules.admin.role.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nookit.common.api.ResultCode;
import com.nookit.common.domain.rbac.Permission;
import com.nookit.common.domain.rbac.Role;
import com.nookit.common.domain.rbac.RolePermission;
import com.nookit.common.exception.BusinessException;
import com.nookit.common.exception.ResourceNotFoundException;
import com.nookit.modules.admin.role.dto.RoleCreateReq;
import com.nookit.modules.admin.role.dto.RoleVO;
import com.nookit.modules.admin.role.mapper.PermissionMapper;
import com.nookit.modules.admin.role.mapper.RoleAdminMapper;
import com.nookit.modules.admin.role.mapper.RolePermissionMapper;
import com.nookit.modules.admin.role.service.RoleAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleAdminServiceImpl implements RoleAdminService {

    private final RoleAdminMapper roleAdminMapper;
    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;

    @Override
    public List<RoleVO> listRoles() {
        List<RoleVO> roles = roleAdminMapper.listRoles();
        roles.forEach(r -> r.setPermissions(permissionMapper.findPermissionCodesByRoleId(r.getId())));
        return roles;
    }

    @Override
    public RoleVO getRoleDetail(Long id) {
        RoleVO vo = roleAdminMapper.findRoleDetail(id);
        if (vo == null) throw new ResourceNotFoundException(ResultCode.RESOURCE_NOT_FOUND);
        vo.setPermissions(permissionMapper.findPermissionCodesByRoleId(id));
        return vo;
    }

    @Override
    public void createRole(RoleCreateReq req) {
        Role role = new Role();
        role.setRoleCode("ROLE_" + req.getName().toUpperCase().replaceAll("\\s+", "_"));
        role.setRoleName(req.getName());
        role.setRoleDescription(req.getDescription());
        role.setRoleStatus("active");
        role.setIsSystem(false);
        roleAdminMapper.insert(role);
    }

    @Override
    public void updateRole(Long id, RoleCreateReq req) {
        Role role = roleAdminMapper.selectById(id);
        if (role == null) throw new ResourceNotFoundException(ResultCode.RESOURCE_NOT_FOUND);
        role.setRoleName(req.getName());
        role.setRoleDescription(req.getDescription());
        roleAdminMapper.updateById(role);
    }

    @Override
    public void deleteRole(Long id) {
        RoleVO vo = roleAdminMapper.findRoleDetail(id);
        if (vo == null) throw new ResourceNotFoundException(ResultCode.RESOURCE_NOT_FOUND);
        if (vo.getUserCount() > 0) {
            throw new BusinessException(ResultCode.RESOURCE_CONFLICT, "该角色下还有用户，无法删除");
        }
        roleAdminMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void savePermissions(Long id, List<String> permissionCodes) {
        // Delete existing
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getRoleId, id));

        if (permissionCodes == null || permissionCodes.isEmpty()) return;

        // Find permission IDs by code
        List<Permission> permissions = permissionMapper.selectList(
                new LambdaQueryWrapper<Permission>()
                        .in(Permission::getPermissionCode, permissionCodes));

        for (Permission p : permissions) {
            RolePermission rp = new RolePermission();
            rp.setRoleId(id);
            rp.setPermissionId(p.getId());
            rolePermissionMapper.insert(rp);
        }
    }

    @Override
    public List<Map<String, Object>> listAllPermissions() {
        return permissionMapper.listAllPermissions();
    }
}
