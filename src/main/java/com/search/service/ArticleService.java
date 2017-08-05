package com.search.service;

import java.util.List;

import com.search.entity.Article;

public interface ArticleService {
	void addArticle(Article article);
	void deleteArticle(Long id);
	Article findArticleById(Long id);
	List<Article> searchArticle(String keyword);
	void updateArticle(Article article);
}
