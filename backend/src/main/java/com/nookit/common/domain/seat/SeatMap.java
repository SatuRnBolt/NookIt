package com.nookit.common.domain.seat;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseTimestampEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 座位平面图（多版本）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("seat_maps")
public class SeatMap extends BaseTimestampEntity {

    private Long studyRoomId;
    private Integer versionNo;
    private String mapStatus;
    private String backgroundUrl;
    private Integer mapWidth;
    private Integer mapHeight;
    private LocalDateTime publishedAt;

    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private Long createdBy;
}
