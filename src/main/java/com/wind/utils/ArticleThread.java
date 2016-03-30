package com.wind.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wind.commons.ServiceResult;
import com.wind.entity.Article;
import com.wind.entity.ArticleLink;
import com.wind.entity.Link;
import com.wind.main.Main;
import com.wind.service.ArticleLinkService;
import com.wind.service.ArticleService;
import com.wind.service.LinkService;

public class ArticleThread implements Runnable{
	private final static Logger logger = LoggerFactory.getLogger(ArticleThread.class);
	private Link link;
	private LinkService linkService;
	private ArticleLinkService articleLinkService;
	private ArticleService articleService;
	
	public ArticleThread(LinkService linkService, 
			ArticleLinkService articleLinkService, 
			ArticleService articleService, 
			Link link) {
		this.articleLinkService = articleLinkService;
		this.linkService = linkService;
		this.articleService = articleService;
		this.link = link;
	}
	
	@Override
	public void run() {
		if(link==null) {
			Main.threadEndNum += 1;
    		return ;
    	}
    	List<Link> tmpList = CSDNLinkUtil.getLinks(link.getUrl());
    	
		if(tmpList==null || tmpList.size()==0) {
			Main.threadEndNum += 1;
			return ;
		}
		//如果以前入库，则从linkList中移除
		for(int i=0; i<tmpList.size(); i++) {
			Link tmp = tmpList.get(i);
			if(tmp!=null) {
				add(tmp);
				tmp.setIsParse(Main.linkIsParse);
				linkService.updateById(tmp.getId(), tmp);
			}
		}
		Main.threadEndNum += 1;
	}
	
	public void add(Link link) {
		Pattern pattern = Pattern.compile("http://blog.csdn.net/.+/article/details/[0-9]+");
		Matcher matcher = pattern.matcher(link.getUrl().trim());

		
		if(!matcher.find()) {//普通 url ，录入 link 集合中
			while(Main.linkLockStatus == true) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			Main.linkLockStatus = true;
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("url", link.getUrl());
			ServiceResult<Link> tmpResult = linkService.find(params);
			if(tmpResult.getList()==null || tmpResult.getList().size()==0) {
				linkService.add(link);
				logger.info(link.getUrl()+":插入 link 集合成功");
			}
			Main.linkLockStatus = false;
		} else {	//article url，解析 article 并录入 article 集合
			while(Main.articleLockStatus == true) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			Main.articleLockStatus = true;
			String url = link.getUrl().substring(matcher.start(), matcher.end());
			link.setUrl(url);
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("original_link", url);
			ServiceResult<Article> tmpResult = articleService.find(params);
			if(tmpResult.getList()==null || tmpResult.getList().size()==0) {
				Article article = CSDNBlogUtil.getArticle(link.getUrl());
				articleService.add(article);
				logger.info(link.getUrl()+":插入 article 集合成功");
			}
			Main.articleLockStatus = false;
		}
	}
}
