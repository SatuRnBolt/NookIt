package com.nookit.common.domain.violation;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseTimestampEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 违约申诉。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("violation_appeals")
public class ViolationAppeal extends BaseTimestampEntity {

    private Long violationId;
    private Long userId;
    private String appealStatus;
    private String appealReason;
    private String evidenceJson;
    private String reviewNote;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private Long reviewedBy;
}
