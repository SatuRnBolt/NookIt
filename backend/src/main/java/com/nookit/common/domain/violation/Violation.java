package com.nookit.common.domain.violation;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseTimestampEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 违约记录。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("violations")
public class Violation extends BaseTimestampEntity {

    private Long reservationId;
    private Long userId;
    private String violationType;
    private String violationStatus;
    private LocalDateTime occurredAt;
    private Integer points;
    private String descriptionText;
    private LocalDateTime revokedAt;
    private Long revokedBy;
}
