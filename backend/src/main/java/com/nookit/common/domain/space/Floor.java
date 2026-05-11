package com.nookit.common.domain.space;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseTimestampEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 楼层。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("floors")
public class Floor extends BaseTimestampEntity {

    private Long buildingId;
    private String floorCode;
    private String floorName;
    private Integer floorNumber;
}
