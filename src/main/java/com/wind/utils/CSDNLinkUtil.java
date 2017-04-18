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

    private static List<String> linkAllUrlList = new ArrayList<>();

//    /**
//     * http 请求获取该URL下的所有URL地址
//     *
//     * @param url
//     * @return
//     */
//    public static List<String> httpGetLinkUrls(String url) {
//        Map<String, String> headers = HttpHeaderUtil.getHeader();
//        JSONObject result = HttpUtil.get(url, headers);
//        if (result != null) {
//            Object obj = result.get("content");
//            if (obj != null) {
//                return parseAllUrlFromHtml(obj.toString());
//            }
//        }
//        return null;
//    }
//
//    /**
//	 * 获取html获取其中的所有URL
//	 *
//	 * @author qianchun  @date 2016年3月24日 下午4:22:39
//	 * @param html
//	 * @return
//	 */
//	public static List<String> parseAllUrlFromHtml(String html) {
//		try {
//			Parser linkParser = new Parser(html);
//			TagNameFilter linkTagFilter = new TagNameFilter("a");
//
//			NodeList linkNodes = linkParser.parse(linkTagFilter);
//			for(int i=0; i<linkNodes.size(); i++) {
//				LinkTag linkTag = (LinkTag) linkNodes.elementAt(i);
//				String linkUrl = linkTag.getAttribute("href");
//				if(StringUtils.isBlank(linkUrl) || linkUrl.length()>100) {
//					continue;
//				}
//				//以http://开头,但不是csdn blog 的域名,则排除
//				if(linkUrl.startsWith("http://")
//						&& !linkUrl.startsWith(Constant.ArticleHomeUrl.CSDNBLOGS)) {
//					continue;
//				}
//
//				//不以http://开头,则默认加上 csdn blog 的域名
//				if(!linkUrl.startsWith("http://") && !linkUrl.startsWith("https://")) {
//					linkUrl = Constant.ArticleHomeUrl.CSDNBLOGS + linkUrl;
//				}
//
//				if(!linkAllUrlList.contains(linkUrl)) {
//					linkAllUrlList.add(linkUrl);
//				}
//			}
//			return linkAllUrlList;
//		} catch (ParserException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}

//    public static void main(String[] args) {
//        List<String> linkList = httpGetLinkUrls("http://blog.csdn.net");
//        System.out.println(JSONArray.fromObject(linkList));
//    }
}
