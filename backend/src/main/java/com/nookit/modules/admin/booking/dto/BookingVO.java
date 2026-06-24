package com.nookit.modules.admin.booking.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "预约记录 VO")
public class BookingVO {

    private Long id;
    private String reservationNo;
    private String studentName;
    private String studentId;
    private String roomName;
    private String seatNo;
    private LocalDate date;
    private String startTime;
    private String endTime;
    private String status;
    private LocalDateTime createdAt;
}
