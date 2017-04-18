package com.wind.rabbitmq;

import com.rabbitmq.client.*;
import com.wind.commons.Constant;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * RabbitmqServer
 *
 * @author qianchun
 * @date 17/4/17
 **/
@Service
public class RabbitmqServer {
    private final static Logger logger = LoggerFactory.getLogger(ArticleProductor.class);

    public static Connection connection;
    public static String userName  = null;
    public static String password = null;
    public static String virtualHost = null;
    public static String host = null;
    public static int port = 0;

    public RabbitmqServer(String host, int port, String userName, String password, String virtualHost) {
        this.host = host;
        this.port = port;
        this.virtualHost = virtualHost;
        this.userName = userName;
        this.password = password;
    }

    /**
     * 获取连接
     * @return
     */
    private static Connection getConnection() {
        try {
            if (connection != null) {
                return connection;
            }
            ConnectionFactory factory = new ConnectionFactory();
            factory.setUsername(userName);
            factory.setPassword(password);
            factory.setVirtualHost(virtualHost);
            factory.setHost(host);
            factory.setPort(port);
            connection = factory.newConnection();
            return connection;
        }
        catch (IOException e) {
            logger.error("创建 connection 异常: ", e);
            return null;
        }
        catch (TimeoutException e) {
            logger.error("创建 connection 异常: ", e);
            return null;
        }
    }

    /**
     * 创建 channel
     * 
     * @return
     */
    private static Channel createChannel() {
        try {
            return getConnection().createChannel();
        }
        catch (IOException e) {
            logger.error("创建 channel 异常: ", e);
            return null;
        }
    }

    /**
     * 关闭 channel connection
     * 
     * @param channel
     */
    private static void closeChannel(Channel channel) {
        try {
            channel.close();
        }
        catch (IOException e) {
            logger.error("关闭 channel 异常: ", e);
        }
        catch (TimeoutException e) {
            logger.error("关闭 channel 异常: ", e);
        }
    }

    /**
     * 发送消息
     * @param exchangeName
     * @param queueName
     * @param routingKey
     * @param json
     */
    public static void sendMessage( String exchangeName, String queueName,
            String routingKey, JSONObject json) {
        if (json == null) {
            return;
        }
        Channel channel = null;
        try {
            channel = createChannel();
            channel.exchangeDeclare(exchangeName, "direct", false);

            //设置交换机
            channel.exchangeDeclare(exchangeName, "direct");
            //设置队列
            channel.queueDeclare(queueName, false, false, false, null);

            //绑定 exchange 和 queue 通过routeKey
            channel.queueBind(queueName, exchangeName,
                    routingKey);

            //发送消息
            channel.basicPublish(exchangeName, routingKey, null, json.toString().getBytes());
        }
        catch (IOException e) {
            logger.error("发送消息异常", e);
            return;
        } finally {
            closeChannel(channel);
        }
    }

    /**
     * 接收消息
     * @param exchangeName
     * @param queueName
     * @param routingKey
     */

    public static void receiveMsg(String exchangeName, String queueName, String routingKey) {
        final Channel channel = createChannel();
        try {
            //设置交换机
            channel.exchangeDeclare(exchangeName, "direct");
            //设置队列
            channel.queueDeclare(queueName, false, false, false, null);
            //绑定 exchange 和 queue 通过routeKey
            channel.queueBind(queueName, exchangeName,
                    Constant.RabbitmqRoutingKey.ARTICLE);

            //解析队列消息
            boolean autoAck = false;
            GetResponse response = channel.basicGet(queueName, autoAck);

            if (response != null) {
                System.out.println("*****************************************************************************");
                System.out.println(new String(response.getBody()));
                System.out.println("*****************************************************************************");

                channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
            }
        }
        catch (IOException e) {
            logger.error("发送消息异常", e);
            return;
        } finally {
            closeChannel(channel);
        }
    }

    public static void main(String[] args) {
        JSONObject json = new JSONObject();
        json.put("id", 1);
        json.put("title", "标题一二三");

        sendMessage(Constant.RabbitmqExchange.ARTICLE_LINK, Constant.RabbitmqQueue.ARTICLE_LINK,
                Constant.RabbitmqRoutingKey.ARTICLE_LINK, json);


//        while(true) {
//            receiveMsg(Constant.RabbitmqExchange.ARTICLE, Constant.RabbitmqQueue.ARTICLE,Constant.RabbitmqRoutingKey.ARTICLE_LINK);
//        }
//        System.out.println("完成");
    }
}
