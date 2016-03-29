package com.wind.utils;

import org.springframework.stereotype.Service;

import com.wind.entity.ArticleLink;
import com.wind.entity.Link;

@Service
public class LinkUtil {
	public static ArticleLink toArticleLink(Link link) {
		if(link==null) return null;
		ArticleLink articleLink = new ArticleLink();
		articleLink.setId(link.getId());
		articleLink.setFrom(link.getFrom());
		articleLink.setIsParse(link.getIsParse());
		articleLink.setUrl(link.getUrl());
		return articleLink;
	}
}
