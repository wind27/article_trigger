package com.wind.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.htmlparser.Parser;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.wind.commons.Constant;
import com.wind.entity.Link;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class CSDNLinkUtil {
	private final static Logger logger = LoggerFactory.getLogger(CSDNLinkUtil.class);
	
	private static List<Link> articleLinkList = new ArrayList<>();
	private static List<Link> linkList = new ArrayList<>();

	public static Map<String, List<Link>> httpGetLink(String url) {
		Map<String, List<Link>> resultMap = new HashMap<>();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36");
		headers.put("Accept-Encoding", "gzip, deflate, sdch");
		headers.put("Accept-Language", "zh-CN,zh;q=0.8");
		JSONObject result = HttpUtil.get(url, headers);
		if(result!=null) {
			Object obj = result.get("content");
			if(obj!=null) {
				resultMap = parseLink(obj.toString());
			}
		}
		return resultMap;
	}
	
	/**
	 * 获取文章信息
	 * 
	 * @author qianchun  @date 2016年3月24日 下午4:22:39
	 * @param html
	 * @return
	 */
	public static Map<String, List<Link>> parseLink(String html) {
		try {
			Map<String, List<Link>> resultMap = new HashMap<>();
			Parser linkParser = new Parser(html);
			TagNameFilter linkTagFilter = new TagNameFilter("a");

			NodeList linkNodes = linkParser.parse(linkTagFilter); 
			for(int i=0; i<linkNodes.size(); i++) {
				LinkTag linkTag = (LinkTag) linkNodes.elementAt(i);
				String linkUrl = linkTag.getAttribute("href");
				
				if(StringUtils.isBlank(linkUrl) || linkUrl.length()>100) {
					continue;
				}
				
				if(!linkUrl.contains(Constant.ArticleHomeUrl.CSDNBLOGS)) {
					linkUrl = Constant.ArticleHomeUrl.CSDNBLOGS + linkUrl;
				}
				
				if(!linkList.contains(linkUrl)) {
					Link link = new Link();
					link.setFrom(Constant.ArticleFrom.CSDNBLOGS);
					link.setUrl(linkUrl);
					link.setIsParse(Constant.LinkIsParse.NO);
					linkList.add(link);
				}
			}
			resultMap.put("link", linkList);
			return resultMap;
		} catch (ParserException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static List<Link> getLinks(String url) {
		Map<String, List<Link>> resultMap = httpGetLink(url);
		if(resultMap==null || resultMap.get("link")==null) {
			return null;
		}
		return resultMap.get("link");
	}
	public static void main(String[] args) {
		List<Link> linkList = getLinks("http://blog.csdn.net");
		System.out.println(JSONArray.fromObject(linkList));
	}
}
