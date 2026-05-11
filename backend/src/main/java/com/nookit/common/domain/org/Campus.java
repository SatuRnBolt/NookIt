package com.nookit.common.domain.org;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseTimestampEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 校区。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("campuses")
public class Campus extends BaseTimestampEntity {

    private String campusCode;
    private String campusName;
    private String campusStatus;
    private String address;
    private String timezoneName;
}
