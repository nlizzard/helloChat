package com.nlizzard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.nlizzard.api.feign.FileMicroServiceFeign;
import com.nlizzard.base.BaseInfoProperties;
import com.nlizzard.enums.Sex;
import com.nlizzard.exceptions.GraceException;
import com.nlizzard.grace.result.GraceJSONResult;
import com.nlizzard.grace.result.ResponseStatusEnum;
import com.nlizzard.mapper.UsersMapper;
import com.nlizzard.pojo.Users;
import com.nlizzard.pojo.vo.UsersVO;
import com.nlizzard.service.UsersService;
import com.nlizzard.tasks.SMSTask;
import com.nlizzard.utils.DesensitizationUtil;
import com.nlizzard.utils.JwtUtil;
import com.nlizzard.utils.LocalDateUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsersServiceImpl extends BaseInfoProperties implements UsersService {

    private final UsersMapper usersMapper;

    private final SMSTask smsTask;

    private final FileMicroServiceFeign fileMicroServiceFeign;

    // 发送短信验证码
    @Override
    public void sendSMSCode(String userIp, String mobile) {
        // 限制该用户的ip在60秒内只能获得一次验证码
        Boolean flag = redis.setnx(MOBILE_SMSCODE + ":" + userIp, mobile,60);

        if(!flag){
            // 60秒内已经获得过验证码了
            GraceException.display(ResponseStatusEnum.SMS_NEED_WAIT_ERROR);
        }

        // 生成 100000 到 999999 之间的随机数
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        smsTask.sendSMSInTask(mobile, String.valueOf(code));

        // 把验证码存入到redis中，用于后续的注册/登录的校验
        redis.set(MOBILE_SMSCODE + ":" + mobile, String.valueOf(code), 10 * 60);
    }

    // 用户注册
    @Override
    public GraceJSONResult userRegistry(String mobile, String smsCode, String nickname,Integer deviceCode) {
        // 1. 从redis中获得验证码进行校验判断是否匹配
        String redisCode = redis.get(MOBILE_SMSCODE + ":" + mobile);
        if (StringUtils.isBlank(redisCode) || !redisCode.equalsIgnoreCase(smsCode)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SMS_CODE_ERROR);
        }

        // 如果用户没有输入昵称，则默认设置为用户138****1234的形式
        if(StringUtils.isBlank(nickname)){
            nickname = "用户" + DesensitizationUtil.commonDisplay(mobile);
        }

        // 2. 根据mobile查询数据库，如果用户存在，则提示不能重复注册
        Users user = queryMobileIfExist(mobile);
        if(user != null){
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_ALREADY_EXIST_ERROR);
        }

        // 2.1 如果查询数据库中用户为空，则表示用户没有注册过，则需要进行用户信息数据的入库
        UsersService proxy = (UsersService)AopContext.currentProxy();
        // 2.2 这里需要使用代理对象来调用方法，才能保证事务能够生效
        user = proxy.createUsers(mobile, nickname);

        // 3. 用户注册成功
        return successRegistryOrLogin(mobile, user,deviceCode);
    }

    // 用户登录
    @Override
    public GraceJSONResult userLogin(String mobile, String smsCode,Integer deviceCode) {
        // 1. 从redis中获得验证码进行校验判断是否匹配
        String redisCode = redis.get(MOBILE_SMSCODE + ":" + mobile);
        if (StringUtils.isBlank(redisCode) || !redisCode.equalsIgnoreCase(smsCode)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SMS_CODE_ERROR);
        }

        // 2. 根据mobile查询数据库
        Users user = queryMobileIfExist(mobile);
        if (user == null) {
            // 2.1 如果查询数据库中用户为空，则表示用户没有注册过，则返回错误信息
            return GraceJSONResult.errorCustom(ResponseStatusEnum.USER_NOT_EXIST_ERROR);
        }
        // 3. 用户登录成功
        return successRegistryOrLogin(mobile, user,deviceCode);
    }

    // 一键注册登录接口，可以同时提供给用户做登录和注册使用调用
    @Override
    public GraceJSONResult userRegistryOrLogin(String mobile, String smsCode, String nickname,Integer deviceCode) {

        // 如果用户没有输入昵称，则默认设置为用户138****1234的形式
        if(StringUtils.isBlank(nickname)){
            nickname = "用户" + DesensitizationUtil.commonDisplay(mobile);
        }

        // 1. 从redis中获得验证码进行校验判断是否匹配
        String redisCode = redis.get(MOBILE_SMSCODE + ":" + mobile);
        if (StringUtils.isBlank(redisCode) || !redisCode.equalsIgnoreCase(smsCode)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.SMS_CODE_ERROR);
        }

        // 2. 根据mobile查询数据库，如果用户存在，则直接登录
        Users user = queryMobileIfExist(mobile);
        if (user == null) {
            // 2.1 如果查询数据库中用户为空，则表示用户没有注册过，则需要进行用户信息数据的入库
            UsersService proxy = (UsersService) AopContext.currentProxy();
            user = proxy.createUsers(mobile, nickname);
        }
        // 3. 用户成功登录或成功注册
        return successRegistryOrLogin(mobile, user,deviceCode);
    }

    /**
     * 用户注册或登录成功后，删除redis中的短信验证码使其失效，
     * 并且设置用户分布式会话，保存用户的token令牌，存储到redis中，最后返回用户数据给前端
     * @param mobile 手机号
     * @param user 用户对象
     * @return GraceJSONResult对象，表示操作结果
     */
    public GraceJSONResult successRegistryOrLogin(String mobile,Users user,Integer deviceCode){
        // 用户注册/登录成功后，删除redis中的短信验证码使其失效
        redis.del(MOBILE_SMSCODE + ":" + mobile);

        String userId = user.getId();

        // 设置用户分布式会话，保存用户的token令牌，存储到redis中
        String uToken = JwtUtil.generateToken(userId);

        // user:token:{userid}:{deviceCode} -> token    token 由id合成

        // 用户可以多端登录，同端只允许登录一台
        String uTokenKey = REDIS_USER_TOKEN + ":" + userId + ":" + deviceCode;
        redis.setByDays(uTokenKey, uToken,2);   // 设置分布式会话

        // 返回用户数据给前端
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(user, usersVO);
        usersVO.setUserToken(uToken);
        usersVO.setTokenKey(uTokenKey);

        return GraceJSONResult.ok(usersVO);
    }

    @Override
    public Users queryMobileIfExist(String mobile) {
        return usersMapper.selectOne(
                new QueryWrapper<Users>()
                        .eq("mobile", mobile)
        );
    }


    @Transactional
    @Override
    // 创建用户信息，并且返回用户对象
    public Users createUsers(String mobile, String nickname) {

        Users user = new Users();

        user.setMobile(mobile);

        String uuid = UUID.randomUUID().toString();
        String[] uuidStr = uuid.split("-");
        String wechatNum = "wx" + uuidStr[0] + uuidStr[1];
        user.setWechatNum(wechatNum);

        String userId = IdWorker.getIdStr();
        user.setId(userId);

        // 仿微信二维码生成
        String wechatNumUrl = getQrCodeUrl(wechatNum, userId);
        user.setWechatNumImg(wechatNumUrl);


        // 用户138****1234
        // DesensitizationUtil 脱敏
        if (StringUtils.isBlank(nickname)) {
            user.setNickname("用户" + DesensitizationUtil.commonDisplay(mobile));
        }else{
            user.setNickname(nickname);
        }
        user.setRealName("");

        user.setSex(Sex.secret.type);
        user.setEmail("");

        user.setBirthday(LocalDateUtils
                .parseLocalDate("1980-01-01",
                        LocalDateUtils.DATE_PATTERN));

        user.setCountry("中国");
        user.setProvince("");
        user.setCity("");
        user.setDistrict("");

        user.setCreatedTime(LocalDateTime.now());
        user.setUpdatedTime(LocalDateTime.now());

        usersMapper.insert(user);

        return user;
    }

    /**
     *  生成微信二维码，返回图片url
     */
    private String getQrCodeUrl(String wechatNumber, String userId) {
        try {
            return fileMicroServiceFeign.generatorQrCode(wechatNumber, userId);
        } catch (Exception e) {
            // throw new RuntimeException(e);
            return null;
        }
    }
}
