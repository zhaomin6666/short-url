package com.zm.shorturl.app.dto;

import lombok.Getter;

/**
 * @author zm
 * @version 1.0
 * @date 2024-01-09
 */
public class ShortUrlServiceDtoEnum {
    /**
     * code类型
     */
    @Getter
    public enum ShortUrlServiceCode {
        SUCCESS(200),
        FAIL(400);

        private int code;

        ShortUrlServiceCode(int code) {
            this.code = code;
        }
    }
}
