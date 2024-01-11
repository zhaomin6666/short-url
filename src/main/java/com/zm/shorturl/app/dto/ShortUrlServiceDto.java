package com.zm.shorturl.app.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author zm
 * @version 1.0
 * @date 2024-01-09
 */
@Getter
@Setter
@AllArgsConstructor
public class ShortUrlServiceDto {
    private int code;
    private String url;
    private String msg;
}
