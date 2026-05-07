package com.nlizzard.netty.mq;

import com.nlizzard.pojo.netty.ChatMsg;
import com.nlizzard.utils.JsonUtils;

public class MessagePublisher {

    // 定义交换机的名字
    public static final String HELLOCHAT_EXCHANGE = "helloChat_exchange";

    // 定义队列的名字
    // 发送信息到消息队列接受并且保存到数据库的路由地址
    public static final String ROUTING_KEY_HELLOCHAT_MSG_SEND = "helloChat.msg.send";

    public static void sendMsgToSave(ChatMsg msg) throws Exception {
        RabbitMQConnectUtils connectUtils = new RabbitMQConnectUtils();
        connectUtils.sendMsg(JsonUtils.objectToJson(msg),
                HELLOCHAT_EXCHANGE,
                ROUTING_KEY_HELLOCHAT_MSG_SEND);
    }


}

