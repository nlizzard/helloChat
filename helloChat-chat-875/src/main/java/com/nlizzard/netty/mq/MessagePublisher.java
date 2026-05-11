package com.nlizzard.netty.mq;

import com.nlizzard.pojo.netty.ChatMsg;
import com.nlizzard.utils.JsonUtils;

public class MessagePublisher {

    // 定义交换机的名字
    public static final String HELLOCHAT_EXCHANGE = "helloChat_exchange";

    // 定义队列的名字
    // 发送信息到消息队列接受并且保存到数据库的路由地址
    public static final String ROUTING_KEY_HELLOCHAT_MSG_SEND = "helloChat.msg.send";

    /**
     * 发送消息到消息队列,后续完成消息保存到数据库
     * @param msg 消息对象
     * @throws Exception 异常
     */
    public static void sendMsgToSave(ChatMsg msg) throws Exception {
        RabbitMQConnectUtils connectUtils = new RabbitMQConnectUtils();
        connectUtils.sendMsg(JsonUtils.objectToJson(msg),
                HELLOCHAT_EXCHANGE,
                ROUTING_KEY_HELLOCHAT_MSG_SEND);
    }

    /**
     * 发送消息到消息队列，netty集群中所有服务器消费，完成消息的发送
     * @param msg 消息内容
     * @throws Exception 异常
     */
    public static void sendMsgToOtherNettyServer(String msg) throws Exception {
        RabbitMQConnectUtils connectUtils = new RabbitMQConnectUtils();
        String fanout_exchange = "fanout_exchange";
        connectUtils.sendMsg(msg, fanout_exchange, "");
    }
}

