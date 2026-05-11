package com.nookit.common.domain.reservation;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseLogEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 签到流水。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("reservation_checkins")
public class ReservationCheckin extends BaseLogEntity {

    private Long reservationId;
    private Long roomCheckinCodeId;
    private String checkinMethod;
    private String submittedCode;
    private String deviceIdentifier;
    private String ipAddress;
    private String checkinResult;
    private String resultMessage;
    private LocalDateTime checkedInAt;
}
