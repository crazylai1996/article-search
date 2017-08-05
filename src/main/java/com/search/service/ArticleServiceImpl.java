package com.search.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.search.entity.Article;
import com.search.mapper.ArticleMapper;
import com.search.utils.LuceneUtil;
import com.search.utils.RedisManager;

@Service
public class ArticleServiceImpl implements ArticleService{

	@Autowired
	private  ArticleMapper articleMapper;
	@Autowired
	private RedisManager redisManager;
	@Autowired
	private LuceneUtil luceneManager;
	
	@Override
	public void addArticle(Article article) {
		try {
			articleMapper.addArticle(article);
			luceneManager.addDocument(article);
			redisManager.setArticle(article.getId(), article);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void deleteArticle(Long id) {
		try {
			redisManager.delArticle(id);
			luceneManager.deleteDocument(id);
			redisManager.addDelKey(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Article findArticleById(Long id) {
		if(redisManager.isDeletedKey(id)){
			return null;
		}
		Article article = redisManager.getArticle(id);
		if(article == null){
			article = articleMapper.findArticleById(id);
			redisManager.setArticle(id, article);
			System.out.println("------数据库中取出！");
		}
		return article;
	}

	@Override
	public List<Article> searchArticle(String keyword) {
		List list = null;
		try {
			list = luceneManager.search(keyword);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	@Override
	public void updateArticle(Article article) {
		try {
			redisManager.setArticle(article.getId(), article);
			luceneManager.updateDocument(article);
			redisManager.addUpdateKey(article.getId());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
