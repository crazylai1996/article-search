package com.search.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.search.entity.Article;
import com.search.service.ArticleService;

@Controller
@RequestMapping("/article")
public class ArticleController {
	
	@Autowired
	private ArticleService articleService;
	
	@RequestMapping("/{id}")
	public ModelAndView getArticle(@PathVariable("id") Long id){
		ModelAndView mav = new ModelAndView("article_details");
		Article article = articleService.findArticleById(id);
		mav.addObject("article", article);
		return mav;
	}
	
	@RequestMapping("/search")
	public ModelAndView searchArticle(String keyword){
		ModelAndView mav = new ModelAndView("searcher");
		List list = articleService.searchArticle(keyword);
		mav.addObject("articles",list);
		return mav;
	}
	
	@RequestMapping("/add")
	@ResponseBody
	public Article addArticle(Article article){
		articleService.addArticle(article);
		return article;
	}
	
	@RequestMapping("/delete/{id}")
	@ResponseBody
	public String deleteArticle(@PathVariable("id")Long id){
		articleService.deleteArticle(id);
		return "success";
	}
	
	@RequestMapping("/update")
	@ResponseBody
	public String updateArticle(Article article){
		articleService.updateArticle(article);
		return "success";
	}
}
