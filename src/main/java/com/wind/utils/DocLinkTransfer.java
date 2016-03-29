package com.wind.utils;


import java.util.ArrayList;
import java.util.List;

import org.bson.Document;

import com.wind.entity.Link;

import net.sf.json.JSONArray;

public class DocLinkTransfer {
	
	/**
	 * document 2 link
	 * 
	 * @author qianchun  @date 2016年3月25日 下午3:21:13
	 * @param doc
	 * @return
	 */
	public static Link document2Link(Document doc) {
		if(doc==null) {
			return null;
		}
		Link link = new Link();
		if(doc.getLong("id")!=null) {
			long id = doc.getLong("id");
			link.setId(id);
		}
		if(doc.getString("url")!=null) {
			String tmp = doc.getString("url");
			link.setUrl(tmp!=null?tmp:"");
		}
		if(doc.getString("from")!=null) {
			String tmp = doc.getString("from");
			link.setFrom(tmp!=null?tmp:"");
		}
		if(doc.getInteger("is_parse")!=null) {
			int tmp = doc.getInteger("is_parse");
			link.setIsParse(tmp);
		}
		
		return link;
	}
	/**
	 * link to document
	 * 
	 * @author qianchun  @date 2016年3月25日 下午3:22:19
	 * @param link
	 * @return
	 */
	public static Document link2Document(Link link) {
		if(link==null) {
			return null;
		}
		Document doc = new Document();
		doc.put("id", link.getId());
		doc.put("url", link.getUrl());
		doc.put("from", link.getFrom());
		doc.put("is_parse", link.getIsParse());
		return doc;
	}
	
	/**
	 * link to document
	 * 
	 * @author qianchun  @date 2016年3月25日 下午3:22:29
	 * @param linkList
	 * @return
	 */
	public static List<Document> link2Document(List<Link> linkList) {
		if(linkList==null) {
			return null;
		}
		List<Document> docList = new ArrayList<Document>();
		for(int i=0; i<linkList.size(); i++) {
			Link link = linkList.get(i);
			Document doc = null;
			if(link!=null) {
				doc = link2Document(link);
			}
			if(doc!=null) {
				docList.add(doc);
			}
		}
		return docList;
	}
	
	/**
	 * document 2 link
	 * @param docList
	 * @return
	 */
	public static List<Link> document2Link(List<Document> docList) {
		if(docList==null) {
			return null;
		}
		Link link = null;
		List<Link> linkList = new ArrayList<Link>();
		for(int i=0; i<docList.size(); i++) {
			Document doc = docList.get(i);
			if(doc!=null) {
				link = document2Link(doc);
			}
			if(link!=null) {
				linkList.add(link);
			}
		}
		return linkList;
	}
}
