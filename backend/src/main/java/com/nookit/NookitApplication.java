package com.nookit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Nookit StudyRoom 后端启动类。
 * <p>
 * MyBatis 的 Mapper 扫描位于 {@link com.nookit.config.MybatisPlusConfig}。
 */
@SpringBootApplication
public class NookitApplication {

    public static void main(String[] args) {
        SpringApplication.run(NookitApplication.class, args);
    }
}
