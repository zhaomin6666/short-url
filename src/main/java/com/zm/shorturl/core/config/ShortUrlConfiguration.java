package com.zm.shorturl.core.config;

import lombok.Getter;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * @author zm
 * @version 1.0
 * @date 2024-01-09
 */
@Configuration
public class ShortUrlConfiguration {
    /**
     * 应用地址，可能是域名
     */
    @Value("${appUrl}")
    @Getter
    private String appUrl;

    /**
     * 容器路径
     */
    @Value("${server.servlet.context-path}")
    @Getter
    private String contextPath;

    /**
     * 生成的链接的地址前缀
     */
    public String getUrlPrefix(){
        return appUrl + contextPath + "/";
    }

    /**
     * 相同短链接情况下添加的固定字符串
     */
    @Value("${shortUrl.salt}")
    @Getter
    private String salt;

    @Value("${shortUrl.useRedis}")
    @Getter
    private boolean useRedis;

    @Value("${shortUrl.generateDifferentShortUrlForSameUrl}")
    @Getter
    private boolean generateDifferentShortUrlForSameUrl;

    @Value("${shortUrl.redisExpireTimeByMinute}")
    @Getter
    private int redisExpireTimeByMinute;
}
