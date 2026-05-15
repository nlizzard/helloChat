package com.nlizzard.service;

import com.nlizzard.grace.result.GraceJSONResult;
import com.nlizzard.pojo.Users;

public interface UsersService {

    /**
     * 发送短信验证码
     * @param userIp 用户IP
     * @param mobile 手机号
     */
    void sendSMSCode(String userIp, String mobile);

    /**
     * 用户注册
     * @param mobile 手机号
     * @param smsCode 短信验证码
     * @param nickname 昵称
     * @param deviceCode 设备类型码
     * @return GraceJSONResult对象，表示操作结果
     */
    GraceJSONResult userRegistry(String mobile, String smsCode, String nickname,Integer deviceCode);

    /**
     * 用户登录
     * @param mobile 手机号
     * @param smsCode 短信验证码
     * @param deviceCode 设备类型码
     * @return GraceJSONResult对象，表示操作结果
     */
    GraceJSONResult userLogin(String mobile, String smsCode,Integer deviceCode);

    /**
     * 一键注册登录接口，可以同时提供给用户做登录和注册使用调用
     * @param mobile 手机号
     * @param smsCode 短信验证码
     * @param nickname 昵称
     * @param deviceCode 设备类型码
     * @return GraceJSONResult对象，表示操作结果
     */
    GraceJSONResult userRegistryOrLogin(String mobile, String smsCode, String nickname,Integer deviceCode);


    /**
     * 判断用户是否存在，如果存在则返回用户信息，否则null
     * @param mobile 手机号
     * @return 用户对象
     */
    Users queryMobileIfExist(String mobile);

    /**
     * 创建用户信息，并且返回用户对象
     * @param mobile 手机号
     * @param nickname 昵称
     * @return 用户对象
     */
     Users createUsers(String mobile, String nickname);
}
