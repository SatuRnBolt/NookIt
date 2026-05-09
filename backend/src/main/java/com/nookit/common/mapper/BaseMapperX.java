package com.nookit.common.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/**
 * Mapper 基类扩展点。当前仅作为统一入口，便于后续追加通用方法（如批量 upsert）。
 *
 * @param <T> Entity
 */
public interface BaseMapperX<T> extends BaseMapper<T> {
}
