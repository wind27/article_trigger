package com.wind.entity;

import java.io.Serializable;

/**
 * 链接Link
 * 
 * @author qianchun
 * @date 2016年3月25日 下午3:13:59
 */
public class Link implements Serializable {
    private static final long serialVersionUID = 1L;
    private long id;
    private String from;
    private String url;
    private int isParse;
    
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public int getIsParse() {
		return isParse;
	}
	public void setIsParse(int isParse) {
		this.isParse = isParse;
	}
}
