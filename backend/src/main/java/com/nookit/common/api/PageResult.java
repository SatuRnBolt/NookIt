package com.nookit.common.api;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用分页响应。
 *
 * @param <T> 列表元素类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页响应体")
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "当前页数据")
    private List<T> records;

    @Schema(description = "总记录数")
    private long total;

    @Schema(description = "当前页码")
    private long pageNum;

    @Schema(description = "每页数量")
    private long pageSize;

    @Schema(description = "总页数")
    private long pages;

    public static <T> PageResult<T> empty() {
        return new PageResult<>(Collections.emptyList(), 0L, 1L, 10L, 0L);
    }

    public static <T> PageResult<T> of(List<T> records, long total, long pageNum, long pageSize) {
        long pages = pageSize == 0 ? 0 : (total + pageSize - 1) / pageSize;
        return new PageResult<>(records, total, pageNum, pageSize, pages);
    }

    /**
     * 从 MyBatis-Plus 分页对象构造。
     */
    public static <T> PageResult<T> from(IPage<T> page) {
        return new PageResult<>(
                page.getRecords(),
                page.getTotal(),
                page.getCurrent(),
                page.getSize(),
                page.getPages()
        );
    }

    /**
     * 从 MyBatis-Plus 分页对象构造，并将 entity 映射为 DTO。
     */
    public static <E, T> PageResult<T> from(IPage<E> page, Function<E, T> converter) {
        List<T> records = page.getRecords().stream().map(converter).collect(Collectors.toList());
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize(), page.getPages());
    }
}
