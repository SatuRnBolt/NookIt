package com.nookit.common.domain.feedback;

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
@TableName("user_feedbacks")
public class UserFeedback extends BaseTimestampEntity {

    private Long userId;
    private String studentName;
    private String studentNo;
    private String feedbackType;
    private String title;
    private String content;
    private String feedbackStatus;
    private String replyContent;
    private Long repliedBy;
    private LocalDateTime repliedAt;

    @TableLogic(value = "null", delval = "NOW()")
    @TableField(value = "deleted_at", select = false)
    private LocalDateTime deletedAt;
}
