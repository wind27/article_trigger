package com.wind.rabbitmq;

import com.wind.commons.Constant;
import com.wind.entity.Article;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * ArticleProductor
 *
 * @author qianchun
 * @date 17/4/17
 **/
public class ArticleProductor {
    private final static Logger logger = LoggerFactory.getLogger(ArticleProductor.class);

    @Autowired
    private RabbitmqServer rabbitmqServer;

    public static void main(String[] args) {
        Article article = new Article();
        new ArticleProductor().rabbitmqServer.sendMessage(Constant.RabbitmqExchange.ARTICLE,
                Constant.RabbitmqQueue.ARTICLE, Constant.RabbitmqRoutingKey.ARTICLE, JSONObject.fromObject(article));
    }
}
