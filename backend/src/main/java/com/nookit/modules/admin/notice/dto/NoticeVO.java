package com.nookit.modules.admin.notice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "公告 VO")
public class NoticeVO {

    private Long id;
    private String title;
    private String content;
    private String type;
    private String status;
    private Boolean pinned;
    private String author;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
}
