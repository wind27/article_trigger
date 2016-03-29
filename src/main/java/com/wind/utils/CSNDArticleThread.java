package com.wind.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wind.commons.ServiceResult;
import com.wind.entity.ArticleLink;
import com.wind.entity.Link;
import com.wind.main.Main;
import com.wind.service.ArticleLinkService;
import com.wind.service.LinkService;

public class CSNDArticleThread implements Runnable{
	private final static Logger logger = LoggerFactory.getLogger(CSNDArticleThread.class);
	private Link link;
	private LinkService linkService;
	private ArticleLinkService articleLinkService;
	
	public CSNDArticleThread(LinkService linkService, ArticleLinkService articleLinkService, Link link) {
		this.articleLinkService = articleLinkService;
		this.linkService = linkService;
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
		if(Main.linkFlag==false) {
			Main.linkFlag = true;
			
			Pattern pattern = Pattern.compile("http://blog.csdn.net/[0-9a-zA-Z]+/article/details/[0-9]+");
			Matcher matcher = pattern.matcher(link.getUrl().trim());
			if(matcher.find()) {
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("url", link.getUrl());
				ServiceResult<ArticleLink> tmpResult = articleLinkService.find(params);
				if(tmpResult.getList()==null || tmpResult.getList().size()==0) {
					articleLinkService.add(LinkUtil.toArticleLink(link));
					logger.info(link.getUrl()+":添加 article_link 库成功");
				}
			} else {
				Map<String, Object> params = new HashMap<String, Object>();
				params.put("url", link.getUrl());
				ServiceResult<Link> tmpResult = linkService.find(params);
				if(tmpResult.getList()==null || tmpResult.getList().size()==0) {
					linkService.add(link);
					logger.info(link.getUrl()+":添加 link 库成功");
				}
			}
			Main.linkFlag = false;
		}
	}
}
