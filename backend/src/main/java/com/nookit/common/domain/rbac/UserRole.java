package com.nookit.common.domain.rbac;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseTimestampEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户-角色绑定（支持作用域）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_roles")
public class UserRole extends BaseTimestampEntity {

    private Long userId;
    private Long roleId;
    private String scopeType;
    private Long campusId;
    private Long organizationId;
    private Boolean isPrimary;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
    private Long assignedBy;
}
