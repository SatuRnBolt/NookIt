package com.nookit.common.domain.seat;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseTimestampEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 座位临时状态窗口（维护、停用、仅预约等）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("seat_status_windows")
public class SeatStatusWindow extends BaseTimestampEntity {

    private Long seatId;
    private String windowStatus;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
    private String reasonText;

    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private Long createdBy;
}
