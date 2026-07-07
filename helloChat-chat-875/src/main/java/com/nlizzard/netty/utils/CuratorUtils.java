package com.nlizzard.netty.utils;

import com.nlizzard.netty.config.RuntimeConfig;
import lombok.Getter;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * curator客户端工具类
 */
public class CuratorUtils {

    private static final String host = RuntimeConfig.zookeeperHost();    // 单机/集群的ip:port地址
    private static final Integer connectionTimeoutMs = 30 * 1000;        // 连接超时时间
    private static final Integer sessionTimeoutMs = 3 * 1000;            // 会话超时时间
    private static final Integer sleepMsBetweenRetry = 2 * 1000;         // 每次重试的间隔时间
    private static final Integer maxRetries = 3;                         // 最大重试次数
    private static final String namespace = "helloChat-IM";                 // 命名空间（root根节点名称）

    @Getter
    private static final CuratorFramework client;

    static {
        RetryPolicy backoffRetry = new ExponentialBackoffRetry(sleepMsBetweenRetry, maxRetries);

        // 声明初始化客户端
        client = CuratorFrameworkFactory.builder()
                .connectString(host)
                .connectionTimeoutMs(connectionTimeoutMs)
                .sessionTimeoutMs(sessionTimeoutMs)
                .retryPolicy(backoffRetry)
                .namespace(namespace)
                .build();
        client.start();     // 启动curator客户端
    }

}
