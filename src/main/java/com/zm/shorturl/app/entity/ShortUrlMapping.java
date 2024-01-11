package com.zm.shorturl.app.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * @author zm
 * @version 1.0
 * @date 2024-01-09
 */
@Data
@TableName("short_url_mapping")
public class ShortUrlMapping {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String shortUrl;
    private String rawUrl;
    private String calculateUrl;
    private Date generateDate;
    private Date expireDate;
    private Long limitTimes;
    private Long visitTimes;
}
