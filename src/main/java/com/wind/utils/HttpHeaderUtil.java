package com.wind.utils;

import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * HttpHeaderUtil
 *
 * @author qianchun
 * @date 17/4/6
 **/
public class HttpHeaderUtil {

    public static Map<String, String> getHeader() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.86 Safari/537.36");
        headers.put("Accept-Encoding", "gzip, deflate, sdch");
        headers.put("Accept-Language", "zh-CN,zh;q=0.8");
        return headers;
    }
}
