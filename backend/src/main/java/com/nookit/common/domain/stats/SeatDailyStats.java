package com.nookit.common.domain.stats;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseTimestampEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 座位每日统计。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("seat_daily_stats")
public class SeatDailyStats extends BaseTimestampEntity {

    private LocalDate statsDate;
    private Long seatId;
    private Integer reservationCount;
    private Integer occupiedHours;
    private Integer checkedInHours;
    private Integer violationCount;
    private BigDecimal heatScore;
}
