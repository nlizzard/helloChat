package com.nlizzard.netty.utils;


import lombok.Getter;
import redis.clients.jedis.ConnectionPoolConfig;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.RedisClient;

import java.time.Duration;

/**
 * jedis连接池工具类
 */
public class RedisClientUtils {

    @Getter
    private static final RedisClient jedisClient;

    static{
        ConnectionPoolConfig poolConfig = new ConnectionPoolConfig();
        // 连接池中最大连接数, 默认8个
        poolConfig.setMaxTotal(8);

        // 连接池中最大空闲连接数, 默认8个
        poolConfig.setMaxIdle(8);
        // 连接池中最小空闲连接数, 默认0
        poolConfig.setMinIdle(0);

        // 连接池中没有可用连接时，是否等待。默认为true
        poolConfig.setBlockWhenExhausted(true);
        // 连接池中没有可用连接时，最大等待时间，超过这个时间就抛出异常。
        poolConfig.setMaxWait(Duration.ofSeconds(30));

        // 连接池中连接空闲时，是否进行连接有效性检查。默认为false
        poolConfig.setTestWhileIdle(true);
        // 连接池中连接空闲时，进行连接有效性检查的时间间隔。默认为30秒
        poolConfig.setTimeBetweenEvictionRuns(Duration.ofSeconds(1));

        DefaultJedisClientConfig clientConfig = DefaultJedisClientConfig.builder()
                .connectionTimeoutMillis(1000)
                .socketTimeoutMillis(1000)
                .password("")
                .database(0)
                .build();

        jedisClient = RedisClient.builder()
                .hostAndPort("192.168.123.2", 6379)
                .poolConfig(poolConfig)
                .clientConfig(clientConfig)
                .build();
    }

}
