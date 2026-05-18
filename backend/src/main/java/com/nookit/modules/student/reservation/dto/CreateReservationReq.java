package com.nookit.modules.student.reservation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank
    @Schema(description = "开始时间 HH:mm，如 07:30")
    private String startTime;

    @NotBlank
    @Schema(description = "结束时间 HH:mm，如 09:00")
    private String endTime;
}
