package com.nookit.common.domain.notice;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseTimestampEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("admin_notices")
public class AdminNotice extends BaseTimestampEntity {

    private String title;
    private String content;
    private String noticeType;
    private String noticeStatus;
    private Boolean isPinned;
    private String authorName;
    private LocalDateTime publishedAt;

    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private Long createdBy;

    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    @TableLogic(value = "null", delval = "NOW()")
    @TableField(value = "deleted_at", select = false)
    private LocalDateTime deletedAt;
}
