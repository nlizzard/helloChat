package com.nlizzard.netty;

import com.nlizzard.netty.mq.RabbitMQConnectUtils;
import com.nlizzard.netty.utils.RedisClientUtils;
import com.nlizzard.netty.utils.ZookeeperUtils;
import com.nlizzard.netty.websocket.WSServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import redis.clients.jedis.RedisClient;

import java.util.List;
import java.util.Map;

/**
 * Netty 服务的启动类(服务器)
 */
public class ChatServer {

    // 默认端口号
    public static final Integer nettyDefaultPort = 875;
    // 在线人数初始值
    public static final String initOnlineCounts = "0";

    public static void main(String[] args) throws Exception {

        // 定义主从线程组
        // 定义主线程池，用于接受客户端的连接，但是不做任何处理
        EventLoopGroup bossGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());
        // 定义从线程池，处理主线程池交过来的任务，真正进行业务处理的线程池
        EventLoopGroup workerGroup = new MultiThreadIoEventLoopGroup(NioIoHandler.newFactory());

        try {
            // 构建Netty服务器
            ServerBootstrap server = new ServerBootstrap();     // 服务的启动类
            server.group(bossGroup, workerGroup)                // 把主从线程池组放入到启动类中
                    .channel(NioServerSocketChannel.class)      // 设置Nio的双向通道
                    .childHandler(new WSServerInitializer());   // 设置处理器，用于处理workerGroup

            // 启动server，并且绑定端口号，同时启动方式为"同步"
            Integer port = selectPort(nettyDefaultPort);

            // 注册netty服务到zookeeper中
            String ip = ZookeeperUtils.getLocalIp();
            ZookeeperUtils.registerNettyServer("netty_server_list", ip,port);

            // 启动消费者进行监听，队列可以根据动态生成的端口号进行拼接
            String queueName = "netty_queue_" + ip + "_" + port;
            RabbitMQConnectUtils mqConnectUtils = new RabbitMQConnectUtils();
            mqConnectUtils.listen("fanout_exchange", queueName);

            System.out.println("Netty Server 启动了，正在监听端口" + port);
            ChannelFuture channelFuture = server.bind(port).sync();

            // 监听关闭的channel
            channelFuture.channel().closeFuture().sync();
            System.out.println("Netty Server 关闭了。");
        } finally {
            // 优雅关闭线程池组
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * 选择一个netty可用的端口号
     * @param port 端口号
     * @return 可用的端口号
     */
    public static Integer selectPort(Integer port) throws Exception {
        String portKey = "netty_port";
        RedisClient jedis = RedisClientUtils.getJedisClient();
        Map<String, String> portMap = jedis.hgetAll(portKey);
        // 如果没有端口号，或者端口号列表为空，则直接使用默认端口号
        if(portMap == null || portMap.isEmpty()){
            jedis.hset(portKey, String.valueOf(port), initOnlineCounts);
            return port;
        }
        // 端口号map不为空，则需要累加端口号
        List<Integer> portList = portMap.keySet()
                .stream()
                .map(Integer::valueOf)
                .toList();

        Integer maxPort = portList.stream()
                .max(Integer::compareTo)
                .map(max -> max + 10) // 如果有值，则加10
                .orElseThrow(() -> new Exception("无法找到最大端口号")); // 如果没值，直接抛异常

        jedis.hset(portKey, String.valueOf(maxPort), initOnlineCounts);
        return maxPort;
    }
}
