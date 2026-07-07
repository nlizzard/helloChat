package com.nlizzard.netty.config;

public final class RuntimeConfig {

    private RuntimeConfig() {
    }

    public static String redisHost() {
        return stringValue("hellochat.netty.redis.host", "192.168.123.2");
    }

    public static int redisPort() {
        return intValue("hellochat.netty.redis.port", 6379);
    }

    public static String redisPassword() {
        return stringValue("hellochat.netty.redis.password", "");
    }

    public static int redisDatabase() {
        return intValue("hellochat.netty.redis.database", 0);
    }

    public static String rabbitMqHost() {
        return stringValue("hellochat.netty.rabbitmq.host", "192.168.123.2");
    }

    public static int rabbitMqPort() {
        return intValue("hellochat.netty.rabbitmq.port", 5672);
    }

    public static String rabbitMqUsername() {
        return stringValue("hellochat.netty.rabbitmq.username", "nlizzard");
    }

    public static String rabbitMqPassword() {
        return stringValue("hellochat.netty.rabbitmq.password", "nlizzard");
    }

    public static String rabbitMqVirtualHost() {
        return stringValue("hellochat.netty.rabbitmq.virtual-host", "helloChat");
    }

    public static String zookeeperHost() {
        return stringValue("hellochat.netty.zookeeper.host", "127.0.0.1:2181");
    }

    public static String gatewayBaseUrl() {
        String baseUrl = stringValue("hellochat.netty.gateway.base-url", "http://127.0.0.1:1000");
        return stripTrailingSlash(baseUrl);
    }

    public static String advertisedHost(String fallbackHost) {
        return stringValue("hellochat.netty.advertised-host", fallbackHost);
    }

    public static boolean fixedPort() {
        return booleanValue("hellochat.netty.fixed-port", false);
    }

    private static String stringValue(String key, String defaultValue) {
        String propertyValue = System.getProperty(key);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue.trim();
        }

        String envValue = System.getenv(toEnvName(key));
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }

        return defaultValue;
    }

    private static int intValue(String key, int defaultValue) {
        String value = stringValue(key, "");
        if (value.isBlank()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private static boolean booleanValue(String key, boolean defaultValue) {
        String value = stringValue(key, "");
        if (value.isBlank()) {
            return defaultValue;
        }

        return Boolean.parseBoolean(value);
    }

    private static String toEnvName(String key) {
        return key.toUpperCase()
                .replace('.', '_')
                .replace('-', '_');
    }

    private static String stripTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}
