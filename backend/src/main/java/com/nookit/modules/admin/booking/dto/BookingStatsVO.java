package com.nookit.modules.admin.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "预约统计摘要")
public class BookingStatsVO {

    private long todayCount;
    private long weekCount;
    private long totalCount;
}
