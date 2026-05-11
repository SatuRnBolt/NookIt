package com.nookit.common.domain.ai;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseLogEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * AI 意图识别结果。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_intents")
public class AiIntent extends BaseLogEntity {

    private Long conversationId;
    private Long messageId;
    private String intentCode;
    private BigDecimal confidenceScore;
    private String extractedSlotsJson;
    private String routeStatus;
}
