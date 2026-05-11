package com.nookit.common.domain.ai;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseTimestampEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * AI 助手会话。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("ai_conversations")
public class AiConversation extends BaseTimestampEntity {

    private Long userId;
    private String channel;
    private String sessionToken;
    private String conversationTitle;
    private String conversationStatus;
    private String contextJson;
    private LocalDateTime lastMessageAt;
}
