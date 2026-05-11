package com.nookit.common.domain.space;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseTimestampEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 自习室类型。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("room_types")
public class RoomType extends BaseTimestampEntity {

    private String roomTypeCode;
    private String roomTypeName;
    private String bookingMode;
    private String defaultPolicyJson;
    private String descriptionText;
}
