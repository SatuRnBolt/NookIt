package com.nookit.common.api;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 通用分页请求。具体业务可继承本类追加过滤字段。
 */
@Data
@Schema(description = "通用分页请求")
public class PageRequest {

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    @Schema(description = "页码，从 1 开始", defaultValue = "1")
    @Min(value = 1, message = "页码必须大于等于 1")
    private Integer pageNum = DEFAULT_PAGE_NUM;

    @Schema(description = "每页数量", defaultValue = "10")
    @Min(value = 1, message = "每页条数必须大于等于 1")
    @Max(value = MAX_PAGE_SIZE, message = "每页条数不能超过 " + MAX_PAGE_SIZE)
    private Integer pageSize = DEFAULT_PAGE_SIZE;

    @Schema(description = "排序字段（数据库列名）")
    private String orderBy;

    @Schema(description = "排序方向：asc / desc", defaultValue = "desc")
    private String orderDir = "desc";

    public boolean isAsc() {
        return "asc".equalsIgnoreCase(orderDir);
    }

    public long getOffset() {
        return (long) (pageNum - 1) * pageSize;
    }
}
