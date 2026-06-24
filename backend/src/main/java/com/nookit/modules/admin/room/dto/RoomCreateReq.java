package com.nookit.modules.admin.room.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "新增/编辑自习室请求体")
public class RoomCreateReq {

    @NotBlank(message = "自习室编号不能为空")
    @Schema(description = "自习室编号，如 LIB-301")
    private String roomCode;

    @NotBlank(message = "自习室名称不能为空")
    @Schema(description = "名称")
    private String roomName;

    @Schema(description = "展示名称")
    private String displayName;

    @NotNull(message = "校区 ID 不能为空")
    @Schema(description = "校区 ID")
    private Long campusId;

    @NotNull(message = "楼栋 ID 不能为空")
    @Schema(description = "楼栋 ID")
    private Long buildingId;

    @Schema(description = "楼层 ID")
    private Long floorId;

    @Schema(description = "所属机构 ID")
    private Long ownerOrganizationId;

    @NotNull(message = "自习室类型 ID 不能为空")
    @Schema(description = "自习室类型 ID")
    private Long roomTypeId;

    @NotNull(message = "总容量不能为空")
    @Min(value = 1, message = "总容量至少为 1")
    @Schema(description = "总座位数")
    private Integer totalCapacity;

    @Schema(description = "可见范围：public / organization / custom，默认 public")
    private String visibilityScope = "public";

    @Schema(description = "开放规则类型：fixed / weekly_schedule / custom，默认 weekly_schedule")
    private String openRuleType = "weekly_schedule";

    @Schema(description = "状态：draft / active / inactive / closed，默认 active")
    private String roomStatus = "active";

    @Schema(description = "位置说明")
    private String locationDetail;

    @Schema(description = "自习室描述")
    private String descriptionText;
}
