package com.nookit.common.domain.org;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseTimestampEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalTime;

/**
 * 用户消息偏好。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_notification_preferences")
public class UserNotificationPreference extends BaseTimestampEntity {

    private Long userId;
    private String channel;
    private String eventType;
    private Boolean isEnabled;
    private LocalTime quietHoursStart;
    private LocalTime quietHoursEnd;
}
