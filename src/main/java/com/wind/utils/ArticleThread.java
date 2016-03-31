package com.wind.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wind.commons.Constant;
import com.wind.commons.ThreadshareData;
import com.wind.commons.ServiceResult;
import com.wind.entity.Article;
import com.wind.entity.Link;
import com.wind.main.Main;
import com.wind.service.ArticleLinkService;
import com.wind.service.ArticleService;
import com.wind.service.LinkService;

public class ArticleThread implements Runnable{
	private final static Logger logger = LoggerFactory.getLogger(ArticleThread.class);
	private Link link;
	private LinkService linkService;
	private ArticleService articleService;
	
	private List<String> articleUrl = new ArrayList<>();
	private List<String> linkUrl = new ArrayList<>();
	
	public ArticleThread(LinkService linkService, 
			ArticleLinkService articleLinkService, 
			ArticleService articleService, 
			Link link) {
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
    	List<String> tmpList = CSDNLinkUtil.getLinks(link.getUrl());
    	
		if(tmpList==null || tmpList.size()==0) {
			Main.threadEndNum += 1;
			return ;
		}
		//設置url
		setUrl(tmpList);
		add();
		Main.threadEndNum += 1;
	}
	
	/**
	 * 設置url
	 * 
	 * @author qianchun  @date 2016年3月31日 下午6:47:47
	 * @param linkList
	 */
	public void setUrl(List<String> linkList) {
		for(int i=0; i<linkList.size(); i++) {
			if(StringUtils.isBlank(linkList.get(i))) {
				continue;
			}
			String url = linkList.get(i).trim();
			
			Pattern pattern = Pattern.compile("http://blog.csdn.net/.+/article/details/[0-9]+");
			Matcher matcher = pattern.matcher(url);
			
			if(!articleUrl.contains(url) && matcher.find()) {//article url，解析 article 并录入 article 集合
				url = url.substring(matcher.start(), matcher.end());
				articleUrl.add(url);
				url = url.substring(0, url.indexOf("/article"));
			}
			if(!linkUrl.contains(url)) {
				linkUrl.add(url);
			}
		}
	}
	public void add() {
		while(ThreadshareData.articleLockStatus == true) {
			try {
				Thread.sleep(10);
				System.out.println(" article lock 等待中");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//加锁, 并录入 article 数据, 然后解锁
		synchronized (ThreadshareData.class) {
			for(int i=0; i<articleUrl.size(); i++) {
				String url = articleUrl.get(i);
				
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("original_link", url);
				ServiceResult<Article> tmpResult = articleService.find(params);
				
				if(tmpResult.getList()==null || tmpResult.getList().size()==0) {
					Article article = CSDNBlogUtil.getArticle(url);
					articleService.add(article);
					logger.info(url+":插入 article 集合成功");
				} else {
					logger.info(url+":已保存");
				}
			}
			ThreadshareData.articleLockStatus = true;
		}
		while(ThreadshareData.linkLockStatus == true) {
			try {
				Thread.sleep(10);
				System.out.println(" link lock 等待中");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		synchronized (ThreadshareData.class) {
			////加锁, 并录入 link 数据, 然后解锁
			ThreadshareData.linkLockStatus = true;
			for(int i=0; i<linkUrl.size(); i++) {
				String url = linkUrl.get(i);
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("url", url);
				ServiceResult<Link> tmpResult = linkService.find(params);
				if(tmpResult.getList()==null || tmpResult.getList().size()==0) {
					Link link  = new Link();
					link.setUrl(url);
					link.setIsParse(Constant.LinkIsParse.NO);
					link.setFrom(Constant.ArticleFrom.CSDNBLOGS);
					linkService.add(link);
					logger.info(url+":插入 link 集合成功");
				} else {
					logger.info(url+":已保存");
				}
				
			}
			ThreadshareData.linkLockStatus = false;
		}
	}
	
	public static void main(String[] args) {
		//http://blog.csdn.net/qq_33982693/article/details/51026679
		//http://blog.csdn.net/qq_33982693
		String s = "http://blog.csdn.net/qq_33982693/article/details/51026679";
		System.out.println(s.substring(0, s.indexOf("/article")));
	}
}
