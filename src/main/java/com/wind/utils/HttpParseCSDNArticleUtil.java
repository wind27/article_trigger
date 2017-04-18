package com.wind.utils;

import com.wind.commons.Constant;
import com.wind.commons.Constant.ArticleFrom;
import com.wind.entity.Article;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.HasAttributeFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HttpParseCSDNArticleUtil {
	private final static Logger logger = LoggerFactory.getLogger(HttpParseCSDNArticleUtil.class);


	/**
	 * http 请求获取该URL下的所有URL地址
	 *
	 * @param url
	 * @return
	 */
	public static List<String> httpGetLinkUrls(String url) {
		Map<String, String> headers = HttpHeaderUtil.getHeader();
		JSONObject result = HttpUtil.get(url, headers);
		if (result != null) {
			Object obj = result.get("content");
			if (obj != null) {
				return parseAllUrlFromHtml(obj.toString());
			}
		}
		return null;
	}

	/**
	 * 获取html获取其中的所有URL
	 *
	 * @author qianchun  @date 2016年3月24日 下午4:22:39
	 * @param html
	 * @return
	 */
	public static List<String> parseAllUrlFromHtml(String html) {
		try {
			List<String> linkAllUrlList = new ArrayList<>();

			Parser linkParser = new Parser(html);
			TagNameFilter linkTagFilter = new TagNameFilter("a");

			NodeList linkNodes = linkParser.parse(linkTagFilter);
			for(int i=0; i<linkNodes.size(); i++) {
				LinkTag linkTag = (LinkTag) linkNodes.elementAt(i);
				String linkUrl = linkTag.getAttribute("href");
				if(StringUtils.isBlank(linkUrl) || linkUrl.length()>100) {
					continue;
				}
				//以http://开头,但不是csdn blog 的域名,则排除
				if(linkUrl.startsWith("http://")
						&& !linkUrl.startsWith(Constant.ArticleHomeUrl.CSDNBLOGS)) {
					continue;
				}

				//不以http://开头,则默认加上 csdn blog 的域名
				if(!linkUrl.startsWith("http://") && !linkUrl.startsWith("https://")) {
					linkUrl = Constant.ArticleHomeUrl.CSDNBLOGS + linkUrl;
				}

				if(!linkAllUrlList.contains(linkUrl)) {
					linkAllUrlList.add(linkUrl);
				}
			}
			return linkAllUrlList;
		} catch (ParserException e) {
			e.printStackTrace();
		}
		return null;
	}

	//******************************************************************************************************************

	/**
	 * http 请求获取该URL对应的article
	 *
	 * @param url
	 * @return
     */
	public static Map<String, String> httpGetArticle(String url) {
		Map<String, String> resultMap = new HashMap<>();
		Map<String, String> headers = HttpHeaderUtil.getHeader();
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
	
	public static Article getArticleByUrl(String url) {
		JSONArray emptyArray = new JSONArray();
		Map<String, String> resultMap = httpGetArticle(url);
		if(resultMap==null || resultMap.get("title")==null && resultMap.get("content")==null) {
			return null;
		}
		Article article = new Article();
		article.setTags("tags");
		article.setOriginalLink(url);
		article.setTitle(resultMap.get("title"));
		article.setDesc(getDesc(resultMap.get("content")));
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
	
	/**
	 * 解析 desc
	 * 
	 * @author qianchun  @date 2016年3月30日 下午4:00:41
	 * @param content
	 * @return
	 */
	public static String getDesc(String content) {
		StringBuffer sb = new StringBuffer();
		NodeList nodes = null;
		try {
			Parser linkParser = new Parser(content);
			nodes = linkParser.extractAllNodesThatMatch(new NodeFilter() {
				public boolean accept(Node node) {
					return true;
			    }
			});
		} catch (ParserException e) {
			e.printStackTrace();
			logger.error("解析 desc 异常");
			return "";
		}
		
		if(nodes!=null && nodes.size()>0) {
			for(int i=0; i<nodes.size(); i++) {
				Node node = nodes.elementAt(i);
				if(node==null) {
					continue;
				}
				String text = node.toPlainTextString().trim(); 
				if(sb.length()<120 || !StringUtils.isBlank(text) || !text.trim().startsWith("转载地址")) {
					sb.append(text.replaceAll("\\s*", ""));
				}
			}
		}
		if(sb.toString().length()>200) {
			return sb.toString().substring(0, 200);
		} else {
			return sb.toString();
		}
	}
	public static void main(String[] args) {
		String url = "http://blog.csdn.net/u011680118/article/details/51011220";
		Article article = getArticleByUrl(url);
		System.out.println(JSONObject.fromObject(article).toString());
	}
}
