package com.wind.utils;

import java.util.HashMap;
import java.util.Map;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.wind.commons.Constant;
import com.wind.commons.Constant.ArticleFrom;
import com.wind.entity.Article;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class CSDNBlogUtil {
	private final static Logger logger = LoggerFactory.getLogger(CSDNBlogUtil.class);

	public static Map<String, String> httpGetArticle(String url) {
		Map<String, String> resultMap = new HashMap<>();
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36");
		headers.put("Accept-Encoding", "gzip, deflate, sdch");
		headers.put("Accept-Language", "zh-CN,zh;q=0.8");
		JSONObject result = HttpUtil.get(url, headers);
		if(result!=null) {
			Object obj = result.get("content");
			if(obj!=null) {
				resultMap = parseArticle(obj.toString());
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
	public static Map<String, String> parseArticle(String html) {
		try {
			Map<String, String> resultMap = new HashMap<>();
			Parser titleParser = new Parser(html);
			Parser contentParser = new Parser(html);
			Parser tagParser = new Parser(html);
			AndFilter titleFilter = new AndFilter(new TagNameFilter("span"), 
					new HasAttributeFilter("class","link_title"));
			AndFilter contentFilter = new AndFilter(new TagNameFilter("div"), 
					new HasAttributeFilter("class","article_content"));
			AndFilter tagFilter = new AndFilter(new TagNameFilter("span"), 
					new HasAttributeFilter("class","link_categories"));
			  
			NodeList titleNodes = titleParser.parse(titleFilter); 
			NodeList tagNodes = tagParser.parse(tagFilter); 
			NodeList contentNodes = contentParser.parse(contentFilter);
			for(int i=0; i<titleNodes.size(); i++) {
				Node node = titleNodes.elementAt(i);
				resultMap.put("title", node.toPlainTextString());
			}
			for(int i=0; i<contentNodes.size(); i++) {
				Node node = contentNodes.elementAt(i);
				resultMap.put("content", node.toHtml());
			}
			for(int i=0; i<tagNodes.size(); i++) {
				Node node = tagNodes.elementAt(i);
				resultMap.put("tags", node.toHtml());
			}
			
			return resultMap;
		} catch (ParserException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Article getArticle(String url) {
		JSONArray emptyArray = new JSONArray();
		Map<String, String> resultMap = httpGetArticle(url);
		if(resultMap==null || resultMap.get("title")==null && resultMap.get("content")==null) {
			return null;
		}
		Article article = new Article();
		article.setTags("tags");
		article.setOriginalLink(url);
		article.setTitle(resultMap.get("title"));
		article.setDesc(resultMap.get("content"));
		article.setContent(resultMap.get("content"));
		article.setFrom(ArticleFrom.CSDNBLOGS);
		
		article.setStatus(Constant.ArticleStatus.PUBLISH);
		article.setUid(1001);
		article.setIsDel(Constant.IsDelete.NO);
		article.setViewNum(0);

		long now = System.currentTimeMillis();
		article.setCreateTime(now);
		article.setPublishTime(now);
		article.setUpdateTime(0);

		article.setCollectionUid(emptyArray.toString());
		article.setPraiseUid(emptyArray.toString());
		
		return article;
	}
	public static void main(String[] args) {
		Article article = getArticle("http://blog.csdn.net/mr_tank_/article/details/17454315");
		System.out.println(JSONObject.fromObject(article));
	}
}
