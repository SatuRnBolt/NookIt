package com.nookit.common.domain.notification;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseTimestampEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息模板。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notification_templates")
public class NotificationTemplate extends BaseTimestampEntity {

    private String templateKey;
    private String channel;
    private String eventType;
    private String languageCode;
    private String titleTemplate;
    private String bodyTemplate;
    private Boolean isActive;
}
