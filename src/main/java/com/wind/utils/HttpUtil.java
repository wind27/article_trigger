package com.wind.utils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONObject;

public class HttpUtil {
	private final static Logger logger = LoggerFactory.getLogger(HttpUtil.class);
	
	public String post() {
		return "";
	}
	public static JSONObject get(String url, Map<String, String> headerMap) {
		JSONObject resultJson = new JSONObject();

		HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
		CloseableHttpClient closeableHttpClient = httpClientBuilder.build();
		try {
			HttpGet httpGet = new HttpGet(url);
			if(headerMap!=null) {
				Iterator<String> it = headerMap.keySet().iterator();
				while(it.hasNext()) {
					String key = it.next();
					String value = headerMap.get(key);
					if(!StringUtils.isBlank(key)) {
						httpGet.addHeader(key, value);
					}
				}
			}
			// 执行get请求
			HttpResponse httpResponse = closeableHttpClient.execute(httpGet);
			// 获取响应消息实体
			HttpEntity entity = httpResponse.getEntity();
			// 判断响应实体是否为空
			if (entity != null) {
//				System.out.println("contentEncoding:" + entity.getContentEncoding());
				resultJson.put("content", EntityUtils.toString(entity));
			}
		} catch (IllegalArgumentException e) {
		} catch (IOException e) {
		} finally {
			try { // 关闭流并释放资源
				closeableHttpClient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return resultJson;
	}

	//http://blog.csdn.net/dyllove98/article/list/1
	//http://blog.csdn.net/dyllove98/article/list/2
	//http://blog.csdn.net/dyllove98/article/list/103
	
	//					   dyllove98/article/details/4701743
	//http://blog.csdn.net/dyllove98/article/details/4701743
	public void getArticleList() {
		
	}
	public static void main(String[] args) {
		String url = "http://blog.csdn.net/mr_tank_/article/details/17454315";
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36");
		headers.put("Accept-Encoding", "gzip, deflate, sdch");
		headers.put("Accept-Language", "zh-CN,zh;q=0.8");
		JSONObject result = HttpUtil.get(url, headers);
		System.out.println(result);
	}
}
