package com.nookit.common.domain.audit;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseLogEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 审计日志。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("audit_logs")
public class AuditLog extends BaseLogEntity {

    private Long actorUserId;
    private String requestId;
    private String actionModule;
    private String actionType;
    private String resourceType;
    private Long resourceId;
    private String ipAddress;
    private String userAgent;
    private String beforeJson;
    private String afterJson;
}
