package com.nookit.common.domain.notification;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseTimestampEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 待发送消息任务。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notification_jobs")
public class NotificationJob extends BaseTimestampEntity {

    private Long userId;
    private Long reservationId;
    private Long violationId;
    private Long templateId;
    private String channel;
    private String eventType;
    private String jobStatus;
    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;
    private String dedupeKey;
    private String payloadJson;
    private String failureReason;
}
