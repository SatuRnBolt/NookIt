package com.nookit.common.domain.stats;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseTimestampEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 自习室每日统计。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("room_daily_stats")
public class RoomDailyStats extends BaseTimestampEntity {

    private LocalDate statsDate;
    private Long studyRoomId;
    private Integer reservationCount;
    private Integer activeUserCount;
    private Integer seatHourTotal;
    private Integer seatHourBooked;
    private Integer seatHourCheckedIn;
    private Integer violationCount;
    private BigDecimal utilizationRate;
}
