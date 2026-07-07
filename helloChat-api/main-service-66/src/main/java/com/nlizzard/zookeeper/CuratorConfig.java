package com.nlizzard.zookeeper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nlizzard.base.BaseInfoProperties;
import com.nlizzard.pojo.netty.NettyServerNode;
import com.nlizzard.utils.JsonUtils;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@Component
@ConfigurationProperties(prefix = "zookeeper.curator")
@Data
@EqualsAndHashCode(callSuper = true)
public class CuratorConfig extends BaseInfoProperties {

    @Resource
    private RabbitAdmin rabbitAdmin;

    private String host;                    // 单机/集群的ip:port地址
    private Integer connectionTimeoutMs;    // 连接超时时间
    private Integer sessionTimeoutMs;       // 会话超时时间
    private Integer sleepMsBetweenRetry;    // 每次重试的间隔时间
    private Integer maxRetries;             // 最大重试次数
    private String namespace;               // 命名空间（root根节点名称）

    public static final String path = "/netty_server_list";

    @Bean
    public CuratorFramework curatorClient() {
        RetryPolicy backoffRetry = new ExponentialBackoffRetry(sleepMsBetweenRetry, maxRetries);

        // 声明初始化客户端
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(host)
                .connectionTimeoutMs(connectionTimeoutMs)
                .sessionTimeoutMs(sessionTimeoutMs)
                .retryPolicy(backoffRetry)
                .namespace(namespace)
                .build();
        client.start();     // 启动curator客户端

        // 添加节点状态监听器
        addListener(client,path);

        return client;
    }


    /** *
     * 添加节点状态监听器
     * @param client curator客户端
     * @param path 监听的节点路径
     */
    private void addListener(CuratorFramework client, String path) {
        CuratorCache curatorCache = CuratorCache.build(client, path);
        curatorCache.listenable().addListener((type, oldData, data) -> {
            switch (type.name()) {
                case "NODE_CREATED":
                    log.info("(子)节点创建");
                    break;
                case "NODE_CHANGED":
                    log.info("(子)节点数据变更");
                    break;
                case "NODE_DELETED":
                    log.info("(子)节点删除");

                    NettyServerNode oldNode = null;
                    try {
                        oldNode = JsonUtils.jsonToPojo(new String(oldData.getData()),
                                NettyServerNode.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }

                    // 从redis中删除对应的netty服务器节点信息
                    String oldPort = oldNode.getPort() + "";
                    String portKey = "netty_port";
                    redis.hdel(portKey, oldPort);

                    // 移除残留的消息队列
                    String ip = "";
                    try {
                        ip = InetAddress.getLocalHost().getHostAddress();
                    } catch (UnknownHostException e) {
                        throw new RuntimeException(e);
                    }
                    String queueName = "helloChat_queue_" + ip + "_"+ oldPort;
                    rabbitAdmin.deleteQueue(queueName);

                    break;
                default:
                    break;
            }
        });
            curatorCache.start();
    }
}
