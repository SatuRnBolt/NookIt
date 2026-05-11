package com.nookit.common.domain.space;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseTimestampEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 自习室资产（图片、视频、平面图等）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("room_assets")
public class RoomAsset extends BaseTimestampEntity {

    private Long studyRoomId;
    private String assetKind;
    private String assetName;
    private String assetUrl;
    private String mimeType;
    private Integer sortOrder;
    private String metadataJson;

    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private Long createdBy;
}
