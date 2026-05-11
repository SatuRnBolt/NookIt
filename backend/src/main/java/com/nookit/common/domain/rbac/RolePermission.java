package com.nookit.common.domain.rbac;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseLogEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色-权限关联。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("role_permissions")
public class RolePermission extends BaseLogEntity {

    private Long roleId;
    private Long permissionId;
}
