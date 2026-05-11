package com.nookit.common.domain.rbac;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseLogEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限点。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("permissions")
public class Permission extends BaseLogEntity {

    private String permissionCode;
    private String permissionName;
    private String resourceCode;
    private String actionCode;
    private String permissionDescription;
}
