package com.nookit.common.domain.reservation;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseLogEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 预约状态流转日志。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reservation_status_logs")
public class ReservationStatusLog extends BaseLogEntity {

    private Long reservationId;
    private String fromStatus;
    private String toStatus;
    private String changeSource;
    private Long changedByUserId;
    private String changeNote;
}
