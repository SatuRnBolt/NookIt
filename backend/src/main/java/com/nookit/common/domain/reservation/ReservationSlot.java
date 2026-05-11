package com.nookit.common.domain.reservation;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseLogEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约小时槽（用于唯一性约束防止重叠占座）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reservation_slots")
public class ReservationSlot extends BaseLogEntity {

    private Long reservationId;
    private Long userId;
    private Long seatId;
    private LocalDate slotDate;
    private Integer hourNo;
    private String slotStatus;
    private Integer activeToken;
    private LocalDateTime releasedAt;
}
