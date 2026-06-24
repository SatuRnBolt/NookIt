package com.nookit.modules.admin.role.mapper;

import com.nookit.common.domain.rbac.Permission;
import com.nookit.common.domain.rbac.RolePermission;
import com.nookit.common.mapper.BaseMapperX;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface PermissionMapper extends BaseMapperX<Permission> {

    @Select("SELECT permission_code AS code, permission_name AS name, resource_code AS resource, action_code AS action, permission_description AS description FROM permissions ORDER BY resource_code, action_code")
    List<Map<String, Object>> listAllPermissions();

    @Select("SELECT p.permission_code FROM role_permissions rp JOIN permissions p ON p.id = rp.permission_id WHERE rp.role_id = #{roleId}")
    List<String> findPermissionCodesByRoleId(@Param("roleId") Long roleId);
}
