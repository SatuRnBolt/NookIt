package com.nookit.common.domain.space;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 自习室。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("study_rooms")
public class StudyRoom extends BaseEntity {

    private Long campusId;
    private Long ownerOrganizationId;
    private Long buildingId;
    private Long floorId;
    private Long roomTypeId;
    private String roomCode;
    private String roomName;
    private String displayName;
    private String visibilityScope;
    private String roomStatus;
    private String locationDetail;
    private String descriptionText;
    private Integer totalCapacity;
    private String openRuleType;
    private Integer mapWidth;
    private Integer mapHeight;
    private String policyJson;

    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private Long createdBy;

    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;
}
