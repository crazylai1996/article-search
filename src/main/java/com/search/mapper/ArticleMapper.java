package com.search.mapper;

import java.util.List;

import com.search.entity.Article;

public interface ArticleMapper {
	void addArticle(Article article);
	void deleteArticle(Long id);
	void updateArticle(Article article);
	Article findArticleById(Long id);
	List<Article> findAll();
}
