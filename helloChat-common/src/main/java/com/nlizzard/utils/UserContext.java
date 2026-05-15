package com.nlizzard.utils;

/**
 * 在threadLocal存储用户ID
 */
public class UserContext {
    private static  final ThreadLocal<String> userIdThreadLocal = new ThreadLocal<>();

    /**
     * 将用户ID存储到ThreadLocal中
     * @param userId 用户ID
     */
    public static void setUserId(String userId) {
        userIdThreadLocal.set(userId);
    }

    /**
     * 从ThreadLocal中获取用户ID
     * @return 用户ID
     */
    public static String getUserId() {
        return userIdThreadLocal.get();
    }

    /**
     * 从ThreadLocal中删除用户ID，避免内存泄漏
     */
    public static void removeUserId() {
        userIdThreadLocal.remove();
    }
}
