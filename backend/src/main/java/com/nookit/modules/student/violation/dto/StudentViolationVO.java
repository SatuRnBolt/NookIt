package com.nookit.modules.student.violation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "学生端违约记录 VO")
public class StudentViolationVO {

    private Long id;
    private String roomName;
    private String seatCode;
    private String violationType;
    private String violationStatus;
    private String descriptionText;
    private LocalDateTime occurredAt;
    private LocalDateTime createdAt;
}
