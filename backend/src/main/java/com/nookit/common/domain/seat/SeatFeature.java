package com.nookit.common.domain.seat;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseLogEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 座位特性字典。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("seat_features")
public class SeatFeature extends BaseLogEntity {

    private String featureCode;
    private String featureName;
    private String valueType;
}
