package com.nookit.common.domain.space;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseLogEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 自习室可见性规则。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("room_visibility_rules")
public class RoomVisibilityRule extends BaseLogEntity {

    private Long studyRoomId;
    private String allowType;
    private Long campusId;
    private Long organizationId;
    private Long roleId;
    private Long userId;
}
