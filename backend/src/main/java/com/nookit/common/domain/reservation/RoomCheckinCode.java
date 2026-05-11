package com.nookit.common.domain.reservation;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseTimestampEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 自习室签到码。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("room_checkin_codes")
public class RoomCheckinCode extends BaseTimestampEntity {

    private Long studyRoomId;
    private LocalDate codeDate;
    private String codeValue;
    private String qrPayload;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private String codeStatus;

    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private Long createdBy;
}
