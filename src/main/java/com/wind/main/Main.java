package com.wind.main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.wind.commons.Constant;
import com.wind.commons.Constant.ArticleFrom;
import com.wind.commons.Constant.ArticleHomeUrl;
import com.wind.commons.Constant.ArticleStatus;
import com.wind.commons.ServiceResult;
import com.wind.entity.Link;
import com.wind.service.ArticleLinkService;
import com.wind.service.ArticleService;
import com.wind.service.IdsService;
import com.wind.service.LinkService;
import com.wind.utils.ArticleThread;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"file:target/classes/applicationContext*.xml"})
public class Main {
	private final static Logger logger = LoggerFactory.getLogger(Main.class);
	@Resource
	LinkService linkService;
	
	@Resource
	ArticleLinkService articleLinkService;
	
	@Resource
	ArticleService articleService;
	
	@Resource
	IdsService idsService;
	
	public static int linkIsParse = 1;
	public static int threadStartNum = 0;
	public static int threadEndNum = 0;
	public static boolean linkLockStatus = false;
	public static boolean articleLockStatus = false;
	
	ExecutorService pool = Executors.newFixedThreadPool(20);
	@Test
	public void main() {
		idsService.initMongodbIds();
		
		int start = 0;
		int limit = 20;
		long linkCount = linkService.count(null).getCount();
		if(linkCount==0) {
			List<Link> linkList = new ArrayList<>();
			Link link = new Link();
			link.setIsParse(Constant.LinkIsParse.NO);
			link.setUrl(ArticleHomeUrl.CSDNBLOGS);
			link.setFrom(ArticleFrom.CSDNBLOGS);
			linkList.add(link);
			linkService.batchAdd(linkList);
		}
		while(threadEndNum<=threadStartNum) {
			StringBuffer sb = new StringBuffer();
			sb.append("****************************");
			sb.append("start:"+start+", ");
			sb.append("limit:"+limit + ", ");
			sb.append("已启动线程数:"+threadStartNum+", ");
			sb.append("已完成线程数:"+threadEndNum+", ");
			sb.append("待处理线程数："+(threadStartNum-threadEndNum));
			sb.append("****************************");
			logger.info(sb.toString());
			//分页处理
			Map<String, Object> params = new HashMap<>();
			params.put("pstart", start);
			params.put("plimit", limit);
			ServiceResult<Link> linkResult = linkService.find(params);
			if(!linkResult.getList().isEmpty()) {
				startThreadPool(linkResult.getList());
			}
			start += limit;
		}
		logger.info("threadStartNum: "+threadStartNum+", threadEndNum: "+threadEndNum +", 程序跑完！！！");
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 启动线程池
	 * 
	 * @author qianchun  @date 2016年3月30日 下午4:02:52
	 * @param LinkList
	 */
	public void startThreadPool(List<Link> LinkList) {
		if(LinkList!=null) {
			for(int i=0; i<LinkList.size(); i++) {
				Link tmp = LinkList.get(i);
				if(tmp!=null) {
					while(threadStartNum-threadEndNum > 20) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} 
					threadStartNum += 1;
					Thread t = new Thread(new ArticleThread(linkService, articleLinkService, articleService, tmp));
					pool.execute(t);
				}
			}
		}
	}
}
