package com.nookit.common.domain.notification;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseLogEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 消息投递回执。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notification_deliveries")
public class NotificationDelivery extends BaseLogEntity {

    private Long notificationJobId;
    private String providerName;
    private String providerMessageId;
    private String deliveryStatus;
    private String responseCode;
    private String responseMessage;
    private Integer retryCount;
    private LocalDateTime deliveredAt;
}
