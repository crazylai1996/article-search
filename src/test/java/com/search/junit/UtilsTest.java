package com.search.junit;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.search.utils.LuceneUtil;
import com.search.utils.RedisManager;

public class UtilsTest extends BaseJunit {
	@Autowired
	private LuceneUtil luceneManager;
	@Autowired
	private RedisManager redisManager;
	
	
	@Test
	public void indexCreat(){
		luceneManager.reCreatIndex();
	}
	
	@Test
	public void searchIndex(){
		try {
			List list = luceneManager.search("ÎÄÕÂÀ´Ô´");
//			System.out.println(list.size());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void syncIndex(){
		luceneManager.indexSync();
	}
	
	@Test
	public void syncRedis(){
		redisManager.redisSync();
	}
}
