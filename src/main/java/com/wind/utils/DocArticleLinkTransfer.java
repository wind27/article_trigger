package com.wind.utils;


import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.wind.entity.ArticleLink;

public class DocArticleLinkTransfer {
	
	/**
	 * document 2 articleLink
	 * 
	 * @author qianchun  @date 2016年3月25日 下午3:21:13
	 * @param doc
	 * @return
	 */
	public static ArticleLink document2ArticleLink(Document doc) {
		if(doc==null) {
			return null;
		}
		ArticleLink articleLink = new ArticleLink();
		if(doc.getLong("id")!=null) {
			long id = doc.getLong("id");
			articleLink.setId(id);
		}
		if(doc.getString("url")!=null) {
			String tmp = doc.getString("url");
			articleLink.setUrl(tmp!=null?tmp:"");
		}
		if(doc.getString("from")!=null) {
			String tmp = doc.getString("from");
			articleLink.setFrom(tmp!=null?tmp:"");
		}
		if(doc.getInteger("is_parse")!=null) {
			int tmp = doc.getInteger("is_parse");
			articleLink.setIsParse(tmp);
		}
		
		return articleLink;
	}
	/**
	 * articleLink to document
	 * 
	 * @author qianchun  @date 2016年3月25日 下午3:22:19
	 * @param link
	 * @return
	 */
	public static Document articleLink2Document(ArticleLink articleLink) {
		if(articleLink==null) {
			return null;
		}
		Document doc = new Document();
		doc.put("id", articleLink.getId());
		doc.put("url", articleLink.getUrl());
		doc.put("from", articleLink.getFrom());
		doc.put("is_parse", articleLink.getIsParse());
		return doc;
	}
	
	/**
	 * articleLink to document
	 * 
	 * @author qianchun  @date 2016年3月25日 下午3:22:29
	 * @param linkList
	 * @return
	 */
	public static List<Document> articleLink2Document(List<ArticleLink> articleLinkList) {
		if(articleLinkList==null) {
			return null;
		}
		List<Document> docList = new ArrayList<Document>();
		for(int i=0; i<articleLinkList.size(); i++) {
			ArticleLink articleLink = articleLinkList.get(i);
			Document doc = null;
			if(articleLink!=null) {
				doc = articleLink2Document(articleLink);
			}
			if(doc!=null) {
				docList.add(doc);
			}
		}
		return docList;
	}
	
	/**
	 * document 2 articleLink
	 * @param docList
	 * @return
	 */
	public static List<ArticleLink> document2ArticleLink(List<Document> docList) {
		if(docList==null) {
			return null;
		}
		ArticleLink articleLink = null;
		List<ArticleLink> linkList = new ArrayList<>();
		for(int i=0; i<docList.size(); i++) {
			Document doc = docList.get(i);
			if(doc!=null) {
				articleLink = document2ArticleLink(doc);
			}
			if(articleLink!=null) {
				linkList.add(articleLink);
			}
		}
		return linkList;
	}
}
