package com.nlizzard.netty.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RuntimeConfigTest {

    @AfterEach
    void clearProperties() {
        System.clearProperty("hellochat.netty.redis.host");
        System.clearProperty("hellochat.netty.redis.port");
        System.clearProperty("hellochat.netty.zookeeper.host");
        System.clearProperty("hellochat.netty.gateway.base-url");
        System.clearProperty("hellochat.netty.advertised-host");
        System.clearProperty("hellochat.netty.fixed-port");
    }

    @Test
    void defaultsMatchCurrentDevelopmentAddresses() {
        assertEquals("192.168.123.2", RuntimeConfig.redisHost());
        assertEquals(6379, RuntimeConfig.redisPort());
        assertEquals("192.168.123.2", RuntimeConfig.rabbitMqHost());
        assertEquals(5672, RuntimeConfig.rabbitMqPort());
        assertEquals("127.0.0.1:2181", RuntimeConfig.zookeeperHost());
        assertEquals("http://127.0.0.1:1000", RuntimeConfig.gatewayBaseUrl());
        assertEquals(false, RuntimeConfig.fixedPort());
    }

    @Test
    void systemPropertiesOverrideDefaults() {
        System.setProperty("hellochat.netty.redis.host", "redis");
        System.setProperty("hellochat.netty.redis.port", "6380");
        System.setProperty("hellochat.netty.zookeeper.host", "zookeeper:2181");
        System.setProperty("hellochat.netty.gateway.base-url", "http://gateway-service:1000/");
        System.setProperty("hellochat.netty.advertised-host", "localhost");
        System.setProperty("hellochat.netty.fixed-port", "true");

        assertEquals("redis", RuntimeConfig.redisHost());
        assertEquals(6380, RuntimeConfig.redisPort());
        assertEquals("zookeeper:2181", RuntimeConfig.zookeeperHost());
        assertEquals("http://gateway-service:1000", RuntimeConfig.gatewayBaseUrl());
        assertEquals("localhost", RuntimeConfig.advertisedHost("172.18.0.8"));
        assertEquals(true, RuntimeConfig.fixedPort());
    }
}
