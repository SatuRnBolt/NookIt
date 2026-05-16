package com.nookit.modules.admin.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "自习室列表/详情 VO")
public class RoomVO {

    private Long id;
    private String roomCode;
    private String roomName;
    private String displayName;

    @Schema(description = "校区名称")
    private String campus;

    @Schema(description = "楼栋名称")
    private String building;

    @Schema(description = "楼层名称")
    private String floor;

    @Schema(description = "所属机构名称")
    private String ownerOrganization;

    @Schema(description = "自习室类型名称")
    private String roomType;

    private Integer totalCapacity;
    private String visibilityScope;
    private String roomStatus;
    private String openRuleType;
    private String locationDetail;
    private String descriptionText;
    private LocalDateTime createdAt;

    // IDs for edit form
    private Long campusId;
    private Long buildingId;
    private Long floorId;
    private Long ownerOrganizationId;
    private Long roomTypeId;
}
