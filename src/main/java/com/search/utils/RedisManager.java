package com.search.utils;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.search.entity.Article;
import com.search.mapper.ArticleMapper;

public class RedisManager {
	@Autowired
	private ArticleMapper articleMapper;
	@Autowired
	private RedisTemplate<String,Object> redisTemplate;
	
	public Article getArticle(Long id){
		if(id == null){
			return null;
		}
		ValueOperations<String, Object> valueOper = redisTemplate.opsForValue();
		Object value = valueOper.get("article:"+id);
		if(value == null||!(value instanceof Article)){
			return null;
		}
		return (Article)value;
	}
	
	public void setArticle(Long id,Article value){
		if(id == null||value == null){
			return ;
		}
		ValueOperations< String, Object> valueOper = redisTemplate.opsForValue();
		valueOper.set( "article:"+String.valueOf(id) , value , 48 , TimeUnit.HOURS);//48小时失效
	}
	
	public void delArticle(Long id){
		redisTemplate.delete("article:"+String.valueOf(id));
	}
	
	public void addDelKey(Long id){
		ListOperations<String, Object> listOper = redisTemplate.opsForList();
		listOper.rightPush("to_delete_keys", "article:"+String.valueOf(id));
	}
	
	public boolean isDeletedKey(Long id) {
		ListOperations<String, Object> listOper = redisTemplate.opsForList();
		List keys = listOper.range("to_delete_keys", 0, -1);
		if(keys.contains("article:"+String.valueOf(id))){
			return true;
		}
		return false;
	}
	
	public void addUpdateKey(Long id){
		ListOperations<String, Object> listOper = redisTemplate.opsForList();
		listOper.rightPush("to_update_keys", "article:"+String.valueOf(id));
	}
	
	//同步缓存至数据库
	public void redisSync(){
		ValueOperations<String, Object> valueOper = redisTemplate.opsForValue();
		ListOperations<String, Object> listOper = redisTemplate.opsForList();
		//同步更新
		List keys = listOper.range("to_update_keys", 0, -1);
		Iterator iterator = keys.iterator();
		while (iterator.hasNext()) {
			String key = (String)iterator.next();
			Article article =(Article)valueOper.get(key);
			articleMapper.updateArticle(article);
		}
		redisTemplate.delete("to_update_keys");
		//同步删除
		keys = listOper.range("to_delete_keys", 0, -1);
		iterator = keys.iterator();
		while (iterator.hasNext()) {
			String key = (String)iterator.next();
			articleMapper.deleteArticle(Long.parseLong(key.split(":")[1]));
		}
		redisTemplate.delete("to_delete_keys");
		System.out.println("-----redis同步完成------");
	}
}
