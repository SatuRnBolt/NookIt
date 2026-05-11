package com.nookit.common.domain.stats;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseTimestampEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户每日统计。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_daily_stats")
public class UserDailyStats extends BaseTimestampEntity {

    private LocalDate statsDate;
    private Long userId;
    private Integer reservationCount;
    private Integer checkedInCount;
    private Integer cancelCount;
    private Integer violationCount;
    private LocalDateTime lastActiveAt;
}
