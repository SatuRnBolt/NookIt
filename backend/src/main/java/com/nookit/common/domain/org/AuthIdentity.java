package com.nookit.common.domain.org;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseTimestampEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户身份凭证（账密、微信、OAuth 等）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("auth_identities")
public class AuthIdentity extends BaseTimestampEntity {

    private Long userId;
    private String identityType;
    private String identityKey;
    private String authSource;
    private String credentialHash;
    private String credentialSalt;
    private Integer credentialVersion;
    private Boolean isVerified;
    private LocalDateTime verifiedAt;
    private LocalDateTime lastUsedAt;
}
