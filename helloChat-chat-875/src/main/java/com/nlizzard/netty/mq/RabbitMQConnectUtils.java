package com.nlizzard.netty.mq;

import com.nlizzard.netty.config.RuntimeConfig;
import com.nlizzard.netty.websocket.UserChannelSession;
import com.nlizzard.pojo.netty.DataContent;
import com.nlizzard.utils.JsonUtils;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * RabbitMQ 连接工具。
 *
 * 线程安全与连接管理说明（修复连接泄露）：
 * 1. 连接池（connections）与 ConnectionFactory 提升为进程级 static 单例，所有发送共享同一个池，
 *    不再随每次发送 new 一个实例（原实现里池是实例字段，实例被 GC 后池里的真实 TCP 连接永远不会被 close，
 *    每发一条消息就泄漏一条连接，最终耗尽 RabbitMQ 连接上限导致聊天瘫痪）。
 * 2. 发送方法用 try-finally 保证“取出的连接”一定归还（即使 channel.close() 抛异常也不漏还）。
 * 3. listen（消费者）使用的是阻塞式长连接（basicConsume），必须独占一条连接、不进池、不复用，
 *    否则会与发送逻辑争抢同一条连接导致冲突。
 */
public class RabbitMQConnectUtils {

    // 进程级共享的连接池（static），所有发送复用同一池，避免每条消息新建/泄漏连接
    private static final List<Connection> connections = new ArrayList<>();
    private static final int maxConnection = 20;

    private static final String host = RuntimeConfig.rabbitMqHost();
    private static final int port = RuntimeConfig.rabbitMqPort();
    private static final String username = RuntimeConfig.rabbitMqUsername();
    private static final String password = RuntimeConfig.rabbitMqPassword();
    private static final String virtualHost = RuntimeConfig.rabbitMqVirtualHost();

    private static ConnectionFactory factory;

    public ConnectionFactory getRabbitMqConnection() {
        return getFactory();
    }

    public ConnectionFactory getFactory() {
        initFactory();
        return factory;
    }

    private static synchronized void initFactory() {
        if (factory == null) {
            factory = new ConnectionFactory();
            factory.setHost(host);
            factory.setPort(port);
            factory.setUsername(username);
            factory.setPassword(password);
            factory.setVirtualHost(virtualHost);
        }
    }

    public void sendMsg(String message, String queue) throws Exception {
        Connection connection = getConnection();
        try {
            Channel channel = connection.createChannel();
            try {
                channel.basicPublish("",
                        queue,
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        message.getBytes("utf-8"));
            } finally {
                channel.close();
            }
        } finally {
            // 无论 channel.close() 是否抛异常，都要归还连接，避免泄漏
            setConnection(connection);
        }
    }

    public void sendMsg(String message, String exchange, String routingKey) throws Exception {
        Connection connection = getConnection();
        try {
            Channel channel = connection.createChannel();
            try {
                channel.basicPublish(exchange,
                        routingKey,
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        message.getBytes("utf-8"));
            } finally {
                channel.close();
            }
        } finally {
            setConnection(connection);
        }
    }

    public GetResponse basicGet(String queue, boolean autoAck) throws Exception {
        GetResponse getResponse = null;
        Connection connection = getConnection();
        try {
            Channel channel = connection.createChannel();
            try {
                getResponse = channel.basicGet(queue, autoAck);
            } finally {
                channel.close();
            }
        } finally {
            setConnection(connection);
        }
        return getResponse;
    }

    public Connection getConnection() throws Exception {
        return getAndSetConnection(true, null);
    }

    public void setConnection(Connection connection) throws Exception {
        getAndSetConnection(false, connection);
    }

    private static synchronized Connection getAndSetConnection(boolean isGet, Connection connection) throws Exception {
        initFactory();

        if (isGet) {
            if (connections.isEmpty()) {
                return factory.newConnection();
            }
            Connection newConnection = connections.remove(0);
            if (newConnection.isOpen()) {
                return newConnection;
            } else {
                // 已关闭的连接直接丢弃，重新建一个（原实现这里也会 newConnection，但旧连接未从池中清理）
                return factory.newConnection();
            }
        } else {
            if (connection != null && connection.isOpen() && connections.size() < maxConnection) {
                connections.add(connection);
            }
            // 连接为空/已关闭/池满时不回收，让其被 GC（连接已不再被引用）
            return null;
        }
    }

    /** *
     * 监听消息
     * @param fanout_exchange 交换机名称
     * @param queueName 队列名称
     *
     * 注意：消费者使用阻塞式长连接，这里建的连接独占、不进共享池，也不归还。
     */
    public void listen(String fanout_exchange, String queueName) throws Exception {

        initFactory();
        // 消费者独占一条连接，不走共享池（basicConsume 是阻塞监听，复用发送池会冲突）
        Connection connection = factory.newConnection();
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
