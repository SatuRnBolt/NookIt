package com.nookit.common.domain.system;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseTimestampEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通用字典项。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dictionary_items")
public class DictionaryItem extends BaseTimestampEntity {

    private String dictType;
    private String dictKey;
    private String dictValue;
    private Integer sortOrder;
    private Boolean isSystem;
}
