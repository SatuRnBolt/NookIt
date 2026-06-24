package com.nookit.modules.admin.violation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "违约记录 VO")
public class ViolationVO {

    private Long id;
    private String studentName;
    private String studentId;
    private String roomName;
    private String seatNo;
    private String date;
    private String time;
    private String reason;
    private Integer count;
    private LocalDateTime createdAt;
}
