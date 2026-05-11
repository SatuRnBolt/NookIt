package com.nookit.common.domain.violation;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseLogEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 违约动作日志。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("violation_action_logs")
public class ViolationActionLog extends BaseLogEntity {

    private Long violationId;
    private String actionType;
    private Long operatorUserId;
    private String actionNote;
}
