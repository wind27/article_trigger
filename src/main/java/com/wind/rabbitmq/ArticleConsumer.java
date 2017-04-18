package com.wind.rabbitmq;

import com.wind.commons.Constant;
import com.wind.entity.Article;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * ArticleConsumer
 *
 * @author qianchun
 * @date 17/4/17
 **/
public class ArticleConsumer {
    private final static Logger logger = LoggerFactory.getLogger(ArticleConsumer.class);

    @Autowired
    private RabbitmqServer rabbitmqServer;

    public static void main(String[] args) {
        Article article = new Article();
        new ArticleConsumer().rabbitmqServer.receiveMsg(Constant.RabbitmqExchange.ARTICLE,
                Constant.RabbitmqQueue.ARTICLE, Constant.RabbitmqRoutingKey.ARTICLE);
    }
}
