package com.zm.shorturl;

import com.zm.shorturl.app.dao.mapper.ShortUrlMappingMapper;
import com.zm.shorturl.app.entity.ShortUrlMapping;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

@SpringBootTest
class ShortUrlApplicationTests {

	@Autowired
	private ShortUrlMappingMapper shortUrlMappingMapper;
	@Test
	public void testSelect() {
		System.out.println(("----- selectAll method test ------"));
		List<ShortUrlMapping> userList = shortUrlMappingMapper.selectList(null);
		userList.forEach(System.out::println);
	}
	@Test
	public void testInsert() {
		System.out.println(("----- insert method test ------"));
		ShortUrlMapping record = new ShortUrlMapping();
		record.setShortUrl("1");
		record.setRawUrl("2");
		record.setCalculateUrl("3");
		record.setExpireDate(new Date());
		record.setGenerateDate(new Date());
		record.setLimitTimes(1111111L);
		record.setVisitTimes(2222L);
		shortUrlMappingMapper.insert(record);
	}
}
