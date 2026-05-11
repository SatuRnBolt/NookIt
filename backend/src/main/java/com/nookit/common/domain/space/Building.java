package com.nookit.common.domain.space;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseTimestampEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 楼宇。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("buildings")
public class Building extends BaseTimestampEntity {

    private Long campusId;
    private Long organizationId;
    private String buildingCode;
    private String buildingName;
    private String address;
}
