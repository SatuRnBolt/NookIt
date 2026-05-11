package com.nookit.common.domain.org;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("users")
public class User extends BaseEntity {

    private String userType;
    private String accountStatus;
    private String studentNo;
    private String employeeNo;
    private String fullName;
    private String nickname;
    private String email;
    private String phone;
    private String avatarUrl;
    private Long primaryOrganizationId;
    private Boolean mustChangePassword;
    private Integer violationCount;
    private LocalDateTime lastLoginAt;
    private LocalDateTime lastActiveAt;
    private String remark;
}
