package com.wind.thread;

import com.wind.commons.Constant;
import com.wind.entity.Article;
import com.wind.entity.ArticleLink;
import com.wind.entity.Link;
import com.wind.main.LinkMain;
import com.wind.service.ArticleLinkService;
import com.wind.service.ArticleService;
import com.wind.service.LinkService;
import com.wind.utils.CSDNLinkUtil;
import com.wind.utils.HttpParseCSDNArticleUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkThread implements Runnable {
    private final static Logger logger = LoggerFactory.getLogger(LinkThread.class);

    private Link link;
    private LinkService linkService;
    private ArticleLinkService articleLinkService;
    private ArticleService articleService;
    private Set<String> allUrls = null;

    public LinkThread(LinkService linkService, ArticleLinkService articleLinkService,
            ArticleService articleService, Set<String> allUrls, Link link) {
        this.linkService = linkService;
        this.articleLinkService = articleLinkService;
        this.articleService = articleService;
        this.allUrls = allUrls;
        this.link = link;
    }

    @Override
    public void run() {
        if (link != null) {
            logger.info("启动线程名称:{}, 线程数:{}, 当前url:{}", Thread.currentThread().getName(),
                    LinkMain.threadSize, link.getUrl());

            //解析出该URL下的所有URL
            List<String> urls = HttpParseCSDNArticleUtil.httpGetLinkUrls(link.getUrl());
            if (urls != null && urls.size() > 0) {
                for (int i = 0; i < urls.size(); i++) {
                    String url = urls.get(i);
                    if (StringUtils.isBlank(url)) {
                        continue;
                    }

                    String linkUrl = url.trim();
                    if (linkUrl.endsWith("/")) {
                        linkUrl = linkUrl.substring(0, linkUrl.length() - 1);
                    }

                    if (allUrls.contains(linkUrl)) {
                        continue;
                    }
                    else {
                        allUrls.add(linkUrl);
                    }
                    linkService.insertIfNotExist(linkUrl);
                    logger.debug("当前线程名:{}, 当前URL位置:{}, 总的URL数:{}, URL:{}",
                            Thread.currentThread().getName(), i + 1, urls.size(), linkUrl);
                }

            }
        }
        //过滤csdn文章URL
        String csdnBlogArticleUrl = filterCSDNBlogArticleUrl(link.getUrl());

        boolean articleAddFlag = false;
        //如果URL是 csdn blog 的文章地址, 则爬取文章地址, 并将文章信息录入库中
        if (!StringUtils.isBlank(csdnBlogArticleUrl)) {
            Map<String, Object> params = new HashMap<>();
            params.put("original_link", csdnBlogArticleUrl);
            Article article = articleService.findOne(params);
            if (article == null) {
                article = HttpParseCSDNArticleUtil.getArticleByUrl(csdnBlogArticleUrl);
                if (article != null) {
                    articleAddFlag = articleService.add(article);
                }
            }

            params.clear();
            params.put("url", csdnBlogArticleUrl);
            ArticleLink articleLink = articleLinkService.findOne(params);
            if (articleLink == null) {
                articleLink = new ArticleLink();
                articleLink.setUrl(csdnBlogArticleUrl);
                articleLink.setIsParse(
                        articleAddFlag ? Constant.LINK_IS_PARSE.YES : Constant.LINK_IS_PARSE.NO);
                articleLink.setFrom(Constant.ArticleFrom.CSDNBLOGS);
                articleLinkService.add(articleLink);
            }
        }
        link.setIsParse(Constant.LINK_IS_PARSE.YES);
        linkService.updateById(link.getId(), link);
        LinkMain.threadSize -= 1;

        logger.info("结束线程名称:{}, 线程数:{}, 当前url:{}", Thread.currentThread().getName(),
                LinkMain.threadSize, link.getUrl());
    }

    /**
     * 处理 csdn blog 文章
     * 
     * @author qianchun @date 2016年3月31日 下午6:47:47
     * @param url
     */
    public String filterCSDNBlogArticleUrl(String url) {
        if (StringUtils.isBlank(url)) {
            return null;
        }
        Pattern pattern = Pattern.compile("http://blog.csdn.net/.+/article/details/[0-9]+");
        Matcher matcher = pattern.matcher(url);

        //article url，解析 article 并录入 article 集合
        if (matcher.find()) {
            url = url.substring(matcher.start(), matcher.end());
            return url;
        }
        else {
            return null;
        }
    }

    public static void main(String[] args) {
        //http://blog.csdn.net/qq_33982693/article/details/51026679
        //http://blog.csdn.net/qq_33982693
        String s = "http://blog.csdn.net/qq_33982693/article/details/51026679/haha";
        System.out
                .println(new LinkThread(null, null, null, null, null).filterCSDNBlogArticleUrl(s));

    }
}
