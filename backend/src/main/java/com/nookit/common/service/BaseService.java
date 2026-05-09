package com.nookit.common.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nookit.common.api.ResultCode;
import com.nookit.common.exception.ResourceNotFoundException;

import java.io.Serializable;

/**
 * 业务 Service 顶层接口，对 MyBatis-Plus {@link IService} 的扩展点：
 * <ul>
 *   <li>{@link #getOrThrow}：取不到则抛 {@link ResourceNotFoundException}，避免业务里散写空判断</li>
 * </ul>
 */
public interface BaseService<T> extends IService<T> {

    default T getOrThrow(Serializable id) {
        return getOrThrow(id, ResultCode.RESOURCE_NOT_FOUND);
    }

    default T getOrThrow(Serializable id, ResultCode notFoundCode) {
        T entity = getById(id);
        if (entity == null) {
            throw new ResourceNotFoundException(notFoundCode);
        }
        return entity;
    }
}
