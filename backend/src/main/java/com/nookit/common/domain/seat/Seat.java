package com.nookit.common.domain.seat;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 座位。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("seats")
public class Seat extends BaseEntity {

    private Long studyRoomId;
    private String seatCode;
    private String displayLabel;
    private String seatType;
    private Integer rowNo;
    private Integer colNo;
    private Boolean hasPower;
    private Boolean isWindowSide;
    private Boolean isAccessible;
    private String seatStatus;
    private Boolean isBookable;
    private BigDecimal mapX;
    private BigDecimal mapY;
    private BigDecimal mapWidth;
    private BigDecimal mapHeight;
    private BigDecimal mapRotation;
    private String metadataJson;
}
