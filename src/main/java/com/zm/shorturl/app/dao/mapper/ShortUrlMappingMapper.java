package com.zm.shorturl.app.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zm.shorturl.app.entity.ShortUrlMapping;
import org.apache.ibatis.annotations.Param;

public interface ShortUrlMappingMapper extends BaseMapper<ShortUrlMapping> {

    ShortUrlMapping findByShortUrl(@Param("shortUrl") String shortUrl);

    ShortUrlMapping findByRawUrl(@Param("rawUrl") String rawUrl);
}
