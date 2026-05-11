package com.nookit.common.domain.audit;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseLogEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 业务事件流水（事件溯源 / 统计源数据）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("business_events")
public class BusinessEvent extends BaseLogEntity {

    private String eventType;
    private String aggregateType;
    private Long aggregateId;
    private Long actorUserId;
    private LocalDateTime occurredAt;
    private LocalDate eventDate;
    private String payloadJson;
}
