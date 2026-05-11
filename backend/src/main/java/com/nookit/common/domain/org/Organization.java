package com.nookit.common.domain.org;

import com.baomidou.mybatisplus.annotation.TableName;
import com.nookit.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 组织 / 院系（树形结构）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("organizations")
public class Organization extends BaseEntity {

    private Long campusId;
    private Long parentId;
    private String orgCode;
    private String orgName;
    private String orgType;
    private String orgPath;
    private Integer displayOrder;
    private String orgStatus;
}
