package com.nookit.common.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 数据库实体基类。
 * <p>
 * 含 {@code id} + {@code created_at} + {@code updated_at} + {@code deleted_at}（逻辑删除）。
 * 适用于需要软删除的核心业务表。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class BaseEntity extends BaseTimestampEntity {

    @TableLogic(value = "null", delval = "NOW()")
    @TableField(value = "deleted_at", select = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime deletedAt;
}
