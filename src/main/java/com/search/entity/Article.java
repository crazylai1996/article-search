package com.search.entity;

import java.io.Serializable;

public class Article implements Serializable{
	private static final long serialVersionUID = -1881476974533870467L;
	private Long id;
	private String title;
	private String details;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDetails() {
		return details;
	}
	public void setDetails(String details) {
		this.details = details;
	}
}
