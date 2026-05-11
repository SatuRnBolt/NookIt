package com.nookit.common.domain.ai;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseLogEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 执行动作日志。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_action_logs")
public class AiActionLog extends BaseLogEntity {

    private Long conversationId;
    private Long messageId;
    private String actionType;
    private String targetType;
    private Long targetId;
    private String actionStatus;
    private String requestJson;
    private String responseJson;
}
