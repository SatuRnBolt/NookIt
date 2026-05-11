package com.nookit.common.domain.ai;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseLogEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 助手消息。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_messages")
public class AiMessage extends BaseLogEntity {

    private Long conversationId;
    private String messageRole;
    private String contentText;
    private String intentCode;
    private String referencedEntityType;
    private Long referencedEntityId;
    private String metadataJson;
}
