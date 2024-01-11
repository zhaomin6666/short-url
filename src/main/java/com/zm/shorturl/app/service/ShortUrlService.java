package com.zm.shorturl.app.service;

import com.google.common.hash.Hashing;
import com.zm.shorturl.app.dao.mapper.ShortUrlMappingMapper;
import com.zm.shorturl.app.dto.ShortUrlServiceDto;
import com.zm.shorturl.app.dto.ShortUrlServiceDtoEnum;
import com.zm.shorturl.app.entity.ShortUrlMapping;
import com.zm.shorturl.core.config.ShortUrlConfiguration;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.zm.shorturl.app.dto.ShortUrlServiceDtoEnum.ShortUrlServiceCode;

/**
 * 短链接服务
 * <p>
 * 使用MurMur散列，并转换为62进制
 * 如果重复，那么添加固定字符串再次计算
 *
 * @author zm
 * @version 1.0
 * @date 2023-10-13
 */
@Slf4j
@Component
public class ShortUrlService {

    /**
     * 读取配置
     */
    @Resource
    ShortUrlConfiguration shortUrlConfiguration;

    /**
     * 数据库查询
     */
    @Resource
    private ShortUrlMappingMapper shortUrlMappingMapper;

    /**
     * Redis
     */
    @Resource
    private RedisTemplate<String, Object> redisTemplate;



    /**
     * 六十二进制字符表
     */
    private static final char[] CHAR_SET = "qwertyuiopasdfghjklzxcvbnm0123456789QWERTYUIOPASDFGHJKLZXCVBNM"
            .toCharArray();

    private static final String REDIS_KEY_PREFIX = "SU";
    private static final char REDIS_KEY_SPLIT = ':';

    /**
     * 默认设置14天超时
     */
    private static final long REDIS_KEY_EXPIRE = 14 * 24 * 60 * 60 * 1000L;

    /**
     * 获取短链接并入库保存
     *
     * @param url 长链接
     * @return 短链接
     */
    public ShortUrlServiceDto getShortUrl(String url) {
        return getShortUrl(url, null, null);
    }

    public ShortUrlServiceDto getShortUrl(String url, Date expireDate) {
        return getShortUrl(url, null, expireDate);
    }

    public ShortUrlServiceDto getShortUrl(String url, Long limitTimes) {
        return getShortUrl(url, limitTimes, null);
    }

    /**
     * 获取短链接并入库保存
     *
     * @param url 长链接
     * @return 短链接
     */
    public ShortUrlServiceDto getShortUrl(String url, Long limitTimes, Date expireDate) {
        // 判断长链接是否存在
        ShortUrlMapping shortUrlMappingEntity = shortUrlMappingMapper.findByRawUrl(url);
        if (shortUrlMappingEntity != null) {
            return new ShortUrlServiceDto(ShortUrlServiceDtoEnum.ShortUrlServiceCode.SUCCESS.getCode(),
                    shortUrlConfiguration.getUrlPrefix() + shortUrlMappingEntity.getShortUrl(), "");
        }
        // 生成短链接
        String result = shortenString(url);
        StringBuilder urlBuilder = new StringBuilder(url);
        while (shortUrlMappingMapper.findByShortUrl(result) != null) {
            urlBuilder.append(shortUrlConfiguration.getSalt());
            result = shortenString(urlBuilder.toString());
        }
        // 入库数据
        shortUrlMappingEntity = new ShortUrlMapping();
        shortUrlMappingEntity.setRawUrl(url);
        shortUrlMappingEntity.setCalculateUrl(urlBuilder.toString());
        shortUrlMappingEntity.setShortUrl(result);
        shortUrlMappingEntity.setGenerateDate(new Date());
        shortUrlMappingEntity.setLimitTimes(limitTimes);
        shortUrlMappingEntity.setExpireDate(expireDate);
        shortUrlMappingEntity.setVisitTimes(0L);
        shortUrlMappingMapper.insert(shortUrlMappingEntity);
        // 如果启用redis那么往redis中也插入数据
        if (shortUrlConfiguration.isUseRedis() && redisTemplate != null) {
            redisTemplate.opsForValue().set(REDIS_KEY_PREFIX + REDIS_KEY_SPLIT + result, new ShortUrlValue(url,
                    expireDate, limitTimes).getValueString(), shortUrlConfiguration.getRedisExpireTimeByMinute(),
                    TimeUnit.MINUTES);
        }
        return new ShortUrlServiceDto(ShortUrlServiceDtoEnum.ShortUrlServiceCode.SUCCESS.getCode(),
                shortUrlConfiguration.getUrlPrefix() + result, "");
    }

    public ShortUrlServiceDto getRawUrl(String shortUrl) {
        ShortUrlValue result = null;
        // 开启redis从redis取
        if (shortUrlConfiguration.isUseRedis() && redisTemplate != null) {
            result = new ShortUrlValue((String) redisTemplate.opsForValue().get(REDIS_KEY_PREFIX + REDIS_KEY_SPLIT + shortUrl));
        }
        // redis中没有从数据库里取
        if (result == null) {
            ShortUrlMapping frameShortUrl = shortUrlMappingMapper.findByShortUrl(shortUrl);
            if (frameShortUrl != null) {
                result = new ShortUrlValue(frameShortUrl);

            }
        }
        if (result == null) {
            return new ShortUrlServiceDto(ShortUrlServiceCode.FAIL.getCode(), null, "链接不存在");
        }
        if (result.getExpireDate() != null) {
            // TODO 判断超期
            return new ShortUrlServiceDto(ShortUrlServiceCode.FAIL.getCode(), "", "链接已过期");
        }
        if (result.getLimitTimes() != null) {
            // TODO 判断次数
            if (result.getLimitTimes() > 0) {
                // redis更新
                // 数据库更新
            }
            else {
                return new ShortUrlServiceDto(ShortUrlServiceCode.FAIL.getCode(), "", "超过次数限制");
            }
        }
        return new ShortUrlServiceDto(ShortUrlServiceCode.SUCCESS.getCode(), result.getRawUrl(), "");
    }

    /**
     * 根据长字符串生成6位短字符串
     * 保证相同的长串对应唯一子串
     *
     * @param str 长串
     * @return 短串 6位62进制
     */
    public String shortenString(String str) {
        long hashLong = Hashing.murmur3_32_fixed().hashBytes((str).getBytes(StandardCharsets.UTF_8)).padToLong();
        // 防止出现6位以下的短链接，直接加上62^5=916,132,832
        // 由于6位62位数最大可表示62^6-1=56,800,235,583，而32位散列（无符号位）最大为2^32-1=2,147,483,647
        // 即使加上62^5也不会超过6位
        hashLong += 916132832;
        return hex10To62(hashLong);
    }

    /**
     * 十进制 -> 六十二进制
     *
     * @param num 需要转换的十进制数
     * @return 六十二进制字符串
     */
    public static String hex10To62(long num) {
        StringBuilder result = new StringBuilder();
        while (num > 0) {
            result.append(CHAR_SET[(int) (num % 62)]);
            num /= 62;
        }
        return result.reverse().toString();
    }

    private static class ShortUrlValue {
        private String valueString;
        private String rawUrl;
        private Date expireDate;
        private Long limitTimes;
        private static final char REDIS_VALUE_SPLIT = ':';

        public ShortUrlValue(String rawUrl, Date expireDate, Long limitTimes) {
            this.rawUrl = rawUrl;
            this.expireDate = expireDate;
            this.limitTimes = limitTimes;
            this.valueString = this.rawUrl + REDIS_VALUE_SPLIT
                    + (this.expireDate == null ? "" : this.expireDate.getTime()) + REDIS_VALUE_SPLIT
                    + (this.limitTimes == null ? "" : this.limitTimes);
        }

        public ShortUrlValue(String valueString) {
            this.valueString = valueString;
            String[] split = this.valueString.split(String.valueOf(REDIS_VALUE_SPLIT));
            if (split.length != 3) {
                throw new IllegalArgumentException("值不正确：" + REDIS_VALUE_SPLIT);
            }
            this.rawUrl = split[0];
            this.expireDate = split[1] == null ? null : new Date(Long.parseLong(split[1]));
            this.limitTimes = split[2] == null ? null : Long.parseLong(split[2]);
        }

        public ShortUrlValue(ShortUrlMapping data) {
            this.rawUrl = data.getRawUrl();
            this.expireDate = data.getExpireDate();
            this.limitTimes = data.getLimitTimes();
            this.valueString = this.rawUrl + REDIS_VALUE_SPLIT
                    + (this.expireDate == null ? "" : this.expireDate.getTime()) + REDIS_VALUE_SPLIT
                    + (this.limitTimes == null ? "" : this.limitTimes);
        }

        public String getValueString() {
            return valueString;
        }

        public void setValueString(String valueString) {
            this.valueString = valueString;
        }

        public String getRawUrl() {
            return rawUrl;
        }

        public void setRawUrl(String rawUrl) {
            this.rawUrl = rawUrl;
        }

        public Date getExpireDate() {
            return expireDate;
        }

        public void setExpireDate(Date expireDate) {
            this.expireDate = expireDate;
        }

        public Long getLimitTimes() {
            return limitTimes;
        }

        public void setLimitTimes(Long limitTimes) {
            this.limitTimes = limitTimes;
        }
    }
}
