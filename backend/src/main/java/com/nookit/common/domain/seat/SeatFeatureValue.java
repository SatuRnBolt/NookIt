package com.nookit.common.domain.seat;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseTimestampEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 座位的特性取值。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("seat_feature_values")
public class SeatFeatureValue extends BaseTimestampEntity {

    private Long seatId;
    private Long featureId;
    private Boolean boolValue;
    private BigDecimal numberValue;
    private String textValue;
    private String jsonValue;
}
