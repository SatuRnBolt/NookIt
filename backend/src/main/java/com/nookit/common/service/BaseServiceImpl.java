package com.nookit.common.service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * Service 实现基类。各业务模块的 ServiceImpl 应继承本类。
 *
 * @param <M> Mapper
 * @param <T> Entity
 */
public abstract class BaseServiceImpl<M extends BaseMapper<T>, T>
        extends ServiceImpl<M, T> implements BaseService<T> {
}
