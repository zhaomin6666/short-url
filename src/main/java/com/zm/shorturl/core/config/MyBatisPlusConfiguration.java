package com.zm.shorturl.core.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

/**
 * @author zm
 * @version 1.0
 * @date 2024-01-09
 */
@Configuration
@MapperScan("com.zm.shorturl.app.dao.mapper")
public class MyBatisPlusConfiguration {
}
