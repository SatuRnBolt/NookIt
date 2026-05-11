package com.nookit.common.domain.org;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseTimestampEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户联系方式。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_contact_methods")
public class UserContactMethod extends BaseTimestampEntity {

    private Long userId;
    private String contactType;
    private String contactValue;
    private Boolean isPrimary;
    private Boolean isVerified;
    private LocalDateTime verifiedAt;
}
