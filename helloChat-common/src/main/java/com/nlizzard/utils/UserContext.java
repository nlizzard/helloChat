package com.nlizzard.utils;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 在threadLocal存储用户ID
 */
public class UserContext {
    private static  final ThreadLocal<ConcurrentHashMap<String,String>> userInfoThreadLocal = new ThreadLocal<>();

    /**
     * 将用户ID和redisTokenKey存储到ThreadLocal中
     * @param userId 用户ID
     */
    public static void setUserIdAndRedisTokenKey(String userId,String redisTokenKey) {
        ConcurrentHashMap<String, String> userInfoMap = new ConcurrentHashMap<>();
        userInfoMap.put("userId", userId);
        userInfoMap.put("redisTokenKey", redisTokenKey);
        userInfoThreadLocal.set(userInfoMap);
    }

    /**
     * 从ThreadLocal中获取用户ID
     * @return 用户ID
     */
    public static String getUserId() {
        ConcurrentHashMap<String, String> map = userInfoThreadLocal.get();
        return map.get("userId");
    }

    /**
     * 从ThreadLocal中获取redisTokenKey
     * @return redisTokenKey
     */
    public static String getRedisTokenKey() {
        ConcurrentHashMap<String, String> map = userInfoThreadLocal.get();
        return map.get("redisTokenKey");
    }

    /**
     * 从ThreadLocal中获取用户信息的Map
     * @return 用户信息的Map
     */
    public static ConcurrentHashMap<String, String> getUserInfoMap() {
        return userInfoThreadLocal.get();
    }

    /**
     * 从ThreadLocal中删除用户信息，避免内存泄漏
     */
    public static void removeUserInfo() {
        userInfoThreadLocal.remove();
    }
}
