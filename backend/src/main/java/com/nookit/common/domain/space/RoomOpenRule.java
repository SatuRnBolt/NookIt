package com.nookit.common.domain.space;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseTimestampEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 自习室开放规则。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("room_open_rules")
public class RoomOpenRule extends BaseTimestampEntity {

    private Long studyRoomId;
    private String ruleKind;
    private Integer weekdayNo;
    private LocalDate ruleDate;
    private LocalTime opensAt;
    private LocalTime closesAt;
    private Boolean isOpen;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
}
