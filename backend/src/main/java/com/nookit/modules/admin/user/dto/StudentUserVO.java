package com.nookit.modules.admin.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "学生用户 VO")
public class StudentUserVO {

    private Long id;
    private String name;
    private String studentId;
    private String email;
    private String department;
    private Integer violationCount;
    private String status;
    private LocalDateTime createdAt;
}
