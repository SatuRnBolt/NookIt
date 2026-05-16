package com.nookit.modules.admin.role.mapper;

import com.nookit.common.domain.rbac.Role;
import com.nookit.common.mapper.BaseMapperX;
import com.nookit.modules.admin.role.dto.RoleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RoleAdminMapper extends BaseMapperX<Role> {

    List<RoleVO> listRoles();

    RoleVO findRoleDetail(@Param("id") Long id);
}
