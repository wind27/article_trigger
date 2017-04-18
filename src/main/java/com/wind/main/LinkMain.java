package com.wind.main;

import com.wind.commons.Constant;
import com.wind.entity.Link;
import com.wind.service.ArticleLinkService;
import com.wind.service.ArticleService;
import com.wind.service.IdsService;
import com.wind.service.LinkService;
import com.wind.thread.LinkThread;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * LinkMain
 *
 * @author qianchun
 * @date 17/4/6
 **/
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"file:/Users/qianchun/workspace/article_trigger/target/classes/applicationContext*.xml"})
public class LinkMain {
    private final static Logger logger = LoggerFactory.getLogger(LinkMain.class);

    @Resource
    LinkService linkService;
    @Resource
    ArticleService articleService;
    @Resource
    ArticleLinkService articleLinkService;
    @Resource
    IdsService idsService;
    ExecutorService pool = Executors.newFixedThreadPool(Constant.LINK_MAX_THREAD);

    public static Integer threadSize = 0;
    public static Set<String> allUrls = new HashSet<>();

    @Test
    public void main() {

        idsService.initMongodbIds();

        int start = 0;
        int limit = 20;

        //获取link库中是否有数据, 没有则初始化首页网址
        long linkCount = linkService.count(null);
        if(linkCount==0) {
            Link link = new Link();
            link.setIsParse(Constant.LINK_IS_PARSE.NO);
            link.setUrl(Constant.ArticleHomeUrl.CSDNBLOGS);
            link.setFrom(Constant.ArticleFrom.CSDNBLOGS);
            linkService.add(link);
        }
        //将上次正在解析且未解析完成的状态重置为未解析
        linkService.updateByIsParse(Constant.LINK_IS_PARSE.ING, Constant.LINK_IS_PARSE.NO);

        while(true) {
            //分页处理
            List<Link> links = null;
            while(links==null || links.size()==0) {
                sleep(1000);

                Map<String, Object> params = new HashMap<>();
                params.put("is_parse", Constant.LINK_IS_PARSE.NO);
                links = linkService.findByMap(params, 0, limit);
                logger.info("**************************** start:{}, limit:{}, size:{} ****************************", start, limit, links.size());
            }

            if(links!=null && links.size()>0) {
                for(Link link : links)
                    if (link != null) {
                        while (threadSize >= Constant.LINK_MAX_THREAD) {
                            sleep(1000);
                            continue;
                        }
                        startThreadPool(link);
                    }
            }
        }
    }

    /**
     * 启动线程池
     *
     * @author qianchun  @date 2016年3月30日 下午4:02:52
     * @param link
     */
    public void startThreadPool(Link link) {
        //更改状态
        link.setIsParse(Constant.LINK_IS_PARSE.ING);
        linkService.updateById(link.getId(), link);

        Thread t = new Thread(new LinkThread(linkService, articleLinkService, articleService, allUrls, link));
        t.setName("hehe");
        pool.execute(t);
        LinkMain.threadSize += 1;
    }

    /**
     * 睡眠
     * @param millisecond
     */
    public void sleep(int millisecond) {
        try {
            Thread.sleep(millisecond);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
