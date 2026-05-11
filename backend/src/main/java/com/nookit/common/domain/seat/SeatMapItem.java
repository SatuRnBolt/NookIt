package com.nookit.common.domain.seat;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseTimestampEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 平面图非座位元素（标签、区域、设施、路径）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("seat_map_items")
public class SeatMapItem extends BaseTimestampEntity {

    private Long seatMapId;
    private String itemType;
    private String itemKey;
    private String itemLabel;
    private BigDecimal x;
    private BigDecimal y;
    private BigDecimal width;
    private BigDecimal height;
    private BigDecimal rotationAngle;
    private String styleJson;
}
