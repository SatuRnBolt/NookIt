package com.nookit.common.domain.system;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseTimestampEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 特性开关。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("feature_flags")
public class FeatureFlag extends BaseTimestampEntity {

    private String flagKey;
    private String flagDescription;
    private Boolean isEnabled;
    private String configJson;

    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;
}
