package com.nookit.common.domain.system;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseTimestampEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统配置项。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("system_settings")
public class SystemSetting extends BaseTimestampEntity {

    private String settingKey;
    private String settingGroupName;
    private String valueType;
    private String valueText;
    private String valueJson;
    private String descriptionText;
    private Boolean isPublic;

    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;
}
