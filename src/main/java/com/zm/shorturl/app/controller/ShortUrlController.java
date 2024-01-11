package com.zm.shorturl.app.controller;

import com.zm.shorturl.app.dto.ShortUrlServiceDto;
import com.zm.shorturl.app.dto.ShortUrlServiceDtoEnum;
import com.zm.shorturl.app.service.ShortUrlService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author zm
 * @version 1.0
 * @date 2024-01-09
 */
@RestController
public class ShortUrlController {
    @Resource
    ShortUrlService shortUrlService;

    @RequestMapping("/getShortUrl")
    public String getShortUrl(@RequestParam String url) {
        Assert.hasText(url, "传入url不能为空");
        ShortUrlServiceDto shortUrlServiceDto = shortUrlService.getShortUrl(url);
        if (shortUrlServiceDto.getCode() == ShortUrlServiceDtoEnum.ShortUrlServiceCode.SUCCESS.getCode()) {
            return shortUrlServiceDto.getUrl();
        }
        return shortUrlServiceDto.getMsg();
    }

    @RequestMapping("/s/{shorturl}")
    public String getRawUrl(HttpServletResponse response, @PathVariable("shorturl") String shorturl) throws IOException {
        Assert.hasText(shorturl, "传入短链不能为空");
        ShortUrlServiceDto shortUrlServiceDto = shortUrlService.getRawUrl(shorturl);
        if (shortUrlServiceDto.getCode() == ShortUrlServiceDtoEnum.ShortUrlServiceCode.SUCCESS.getCode()) {
            response.sendRedirect(shortUrlServiceDto.getUrl());
            return "";
        }
        return shortUrlServiceDto.getMsg();
    }
}
