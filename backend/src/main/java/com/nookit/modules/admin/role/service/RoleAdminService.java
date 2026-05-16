package com.nookit.modules.admin.role.service;

import com.nookit.modules.admin.role.dto.RoleCreateReq;
import com.nookit.modules.admin.role.dto.RoleVO;

import java.util.List;
import java.util.Map;

public interface RoleAdminService {

    List<RoleVO> listRoles();

    RoleVO getRoleDetail(Long id);

    void createRole(RoleCreateReq req);

    void updateRole(Long id, RoleCreateReq req);

    void deleteRole(Long id);

    void savePermissions(Long id, List<String> permissionCodes);

    List<Map<String, Object>> listAllPermissions();
}
