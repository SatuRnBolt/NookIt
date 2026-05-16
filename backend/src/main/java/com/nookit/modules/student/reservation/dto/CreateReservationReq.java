package com.nookit.modules.student.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "学生创建预约请求")
public class CreateReservationReq {

    @NotNull
    @Schema(description = "座位ID")
    private Long seatId;

    @NotNull
    @Schema(description = "预约日期 yyyy-MM-dd")
    private String date;

    @Min(7)
    @Max(21)
    @Schema(description = "开始小时 (7-21)")
    private Integer startHour;

    @Min(8)
    @Max(22)
    @Schema(description = "结束小时 (8-22)")
    private Integer endHour;
}
