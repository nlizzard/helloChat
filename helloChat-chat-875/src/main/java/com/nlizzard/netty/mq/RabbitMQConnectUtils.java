package com.nlizzard.netty.mq;

import com.nlizzard.netty.websocket.UserChannelSession;
import com.nlizzard.pojo.netty.DataContent;
import com.nlizzard.utils.JsonUtils;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RabbitMQConnectUtils {

    private final List<Connection> connections = new ArrayList<>();
    private final int maxConnection = 20;

    // 开发环境 dev
    private final String host = "192.168.123.2";
    private final int port = 5672;
    private final String username = "nlizzard";
    private final String password = "nlizzard";
    private final String virtualHost = "helloChat";

    public ConnectionFactory factory;

    public ConnectionFactory getRabbitMqConnection() {
        return getFactory();
    }

    public ConnectionFactory getFactory() {
        initFactory();
        return factory;
    }

    private void initFactory() {
        try {
            if (factory == null) {
                factory = new ConnectionFactory();
                factory.setHost(host);
                factory.setPort(port);
                factory.setUsername(username);
                factory.setPassword(password);
                factory.setVirtualHost(virtualHost);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(String message, String queue) throws Exception {
        Connection connection = getConnection();
        Channel channel = connection.createChannel();
        channel.basicPublish("",
                queue,
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                message.getBytes("utf-8"));
        channel.close();
        setConnection(connection);
    }

    public void sendMsg(String message, String exchange, String routingKey) throws Exception {
        Connection connection = getConnection();
        Channel channel = connection.createChannel();
        channel.basicPublish(exchange,
                routingKey,
                MessageProperties.PERSISTENT_TEXT_PLAIN,
                message.getBytes("utf-8"));
        channel.close();
        setConnection(connection);
    }

    public GetResponse basicGet(String queue, boolean autoAck) throws Exception {
        GetResponse getResponse = null;
        Connection connection = getConnection();
        Channel channel = connection.createChannel();
        getResponse = channel.basicGet(queue, autoAck);
        channel.close();
        setConnection(connection);
        return getResponse;
    }

    public Connection getConnection() throws Exception {
        return getAndSetConnection(true, null);
    }

    public void setConnection(Connection connection) throws Exception {
        getAndSetConnection(false, connection);
    }

    private synchronized Connection getAndSetConnection(boolean isGet, Connection connection) throws Exception {
        getRabbitMqConnection();

        if (isGet) {
            if (connections.isEmpty()) {
                return factory.newConnection();
            }
            Connection newConnection = connections.get(0);
            connections.remove(0);
            if (newConnection.isOpen()) {
                return newConnection;
            } else {
                return factory.newConnection();
            }
        } else {
            if (connections.size() < maxConnection) {
                connections.add(connection);
            }
            return null;
        }
    }

    /** *
     * 监听消息
     * @param fanout_exchange 交换机名称
     * @param queueName 队列名称
     */
    public void listen(String fanout_exchange, String queueName) throws Exception {

        Connection connection = getConnection();
        Channel channel = connection.createChannel();

        // FANOUT 发布订阅模式(广播模式)
        channel.exchangeDeclare(fanout_exchange,
                BuiltinExchangeType.FANOUT,
                true, false, false, null);

        channel.queueDeclare(queueName, true, false, false, null);

        channel.queueBind(queueName, fanout_exchange, "");

        Consumer consumer = new DefaultConsumer(channel){
            /**
             * 重写消息配送方法
             * @param consumerTag 消息的标签（标识）
             * @param envelope  信封（一些信息，比如交换机路由等等信息）
             * @param properties 配置信息
             * @param body 收到的消息数据
             */
            @Override
            public void handleDelivery(String consumerTag,
                                       Envelope envelope,
                                       AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {

                String msg = new String(body);
                System.out.println("body = " + msg);

                String exchange = envelope.getExchange();
                System.out.println("exchange = " + exchange);
                if (exchange.equalsIgnoreCase("fanout_exchange")) {
                    DataContent dataContent = JsonUtils.jsonToPojo(msg, DataContent.class);
                    String senderId = dataContent.getChatMsg().getSenderId();
                    String receiverId = dataContent.getChatMsg().getReceiverId();

                    // 广播至集群的其他节点并且发送给用户聊天信息
                    List<io.netty.channel.Channel> receiverChannels =
                            UserChannelSession.getMultiChannels(receiverId);
                    UserChannelSession.sendToTarget(receiverChannels, dataContent);

                    // 广播至集群的其他节点并且同步给自己其他设备聊天信息
                    String currentChannelId = dataContent.getCurrentChannelId();
                    List<io.netty.channel.Channel> senderChannels =
                            UserChannelSession.getMyOtherChannels(senderId, currentChannelId);
                    UserChannelSession.sendToMyOthers(senderChannels, dataContent);
                }
            }
        };
        /*
          queue: 监听的队列名
          autoAck: 是否自动确认，true：告知mq消费者已经消费的确认通知
          callback: 回调函数，处理监听到的消息
         */
        channel.basicConsume(queueName, true, consumer);
    }
}
