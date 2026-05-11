package com.nookit.common.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 含 {@code id} + {@code created_at} + {@code updated_at} 的基类。
 * <p>
 * 适用于不需要软删除的常规业务表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class BaseTimestampEntity extends BaseLogEntity {

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime updatedAt;
}
