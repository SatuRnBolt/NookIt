package com.nookit.modules.admin.feedback.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "反馈 VO")
public class FeedbackVO {

    private Long id;
    private String studentName;
    private String studentId;
    private String type;
    private String title;
    private String content;
    private String status;
    private String reply;
    private String repliedBy;
    private LocalDateTime repliedAt;
    private LocalDateTime createdAt;
}
