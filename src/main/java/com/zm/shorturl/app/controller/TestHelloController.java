package com.zm.shorturl.app.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试请求
 *
 * @author zm
 * @version 1.0
 * @date 2024-01-09
 */
@RestController
public class TestHelloController {
    @RequestMapping("/hello")
    public String hello() {
        return "hello world";
    }
}
