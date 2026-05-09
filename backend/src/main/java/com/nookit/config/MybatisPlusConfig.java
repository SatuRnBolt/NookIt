package com.nookit.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.nookit.common.util.SecurityContextUtil;
import org.apache.ibatis.reflection.MetaObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 配置：分页插件、乐观锁插件、审计字段自动填充。
 */
@Configuration
@MapperScan("com.nookit.modules.**.mapper")
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor paging = new PaginationInnerInterceptor(DbType.MYSQL);
        paging.setMaxLimit(500L);
        paging.setOverflow(false);
        interceptor.addInnerInterceptor(paging);
        interceptor.addInnerInterceptor(new OptimisticLockerInnerInterceptor());
        return interceptor;
    }

    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                LocalDateTime now = LocalDateTime.now();
                Long userId = SecurityContextUtil.getCurrentUserId();
                strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
                strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
                if (userId != null) {
                    strictInsertFill(metaObject, "createdBy", Long.class, userId);
                    strictInsertFill(metaObject, "updatedBy", Long.class, userId);
                }
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
                Long userId = SecurityContextUtil.getCurrentUserId();
                if (userId != null) {
                    strictUpdateFill(metaObject, "updatedBy", Long.class, userId);
                }
            }
        };
    }
}
