package com.nookit.common.domain.rbac;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseTimestampEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("roles")
public class Role extends BaseTimestampEntity {

    private String roleCode;
    private String roleName;
    private String roleDescription;
    private String roleStatus;
    private Boolean isSystem;
}
