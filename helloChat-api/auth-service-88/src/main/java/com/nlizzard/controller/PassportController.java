package com.nlizzard.controller;

import com.nlizzard.base.BaseInfoProperties;
import com.nlizzard.grace.result.GraceJSONResult;
import com.nlizzard.pojo.bo.RegistryLoginBO;
import com.nlizzard.service.UsersService;
import com.nlizzard.utils.IPUtil;
import com.nlizzard.utils.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("passport")
@Validated
public class PassportController extends BaseInfoProperties {

    private final UsersService usersService;

    /**
     * 获取短信验证码接口
     * @param mobile 手机号
     * @param request HttpServletRequest对象，用于获取用户IP地址
     * @return GraceJSONResult对象，表示操作结果
     */
    @GetMapping("getSMSCode")
    public GraceJSONResult getSMSCode(@NotBlank(message = "手机号不能为空")
                                          @Length(min = 11, max = 11, message = "手机号长度不正确")
                                          @RequestParam("mobile") String mobile,
                                      HttpServletRequest request){

        // 获得用户的ip
        String  userIp = IPUtil.getRequestIp(request);

        // 发送短信验证码
        usersService.sendSMSCode(userIp, mobile);

        return GraceJSONResult.ok();
    }

    /**
     * 注册接口
     * @param registryLoginBO 注册登录BO对象，包含mobile、smsCode、nickname等属性
     * @return GraceJSONResult对象，表示操作结果
     */
    @PostMapping("registry")
    public GraceJSONResult registry(@RequestBody @Valid RegistryLoginBO registryLoginBO){

        String mobile = registryLoginBO.getMobile();
        String code = registryLoginBO.getSmsCode();
        String nickname = registryLoginBO.getNickname();
        Integer deviceCode = registryLoginBO.getDeviceCode();

        //注册用户
        return usersService.userRegistry(mobile, code, nickname,deviceCode);

    }

    @PostMapping("login")
    public GraceJSONResult login(@RequestBody @Valid RegistryLoginBO registryLoginBO){

        String mobile = registryLoginBO.getMobile();
        String code = registryLoginBO.getSmsCode();
        Integer deviceCode = registryLoginBO.getDeviceCode();

        // 登录
        return usersService.userLogin(mobile, code,deviceCode);
    }

    /**
     * 一键注册登录接口，可以同时提供给用户做登录和注册使用调用
     * @param registryLoginBO 注册登录BO对象，包含mobile、smsCode、nickname等属性
     */
    @PostMapping("registryOrLogin")
    public GraceJSONResult registryOrLogin(@RequestBody @Valid RegistryLoginBO registryLoginBO){

        String mobile = registryLoginBO.getMobile();
        String code = registryLoginBO.getSmsCode();
        String nickname = registryLoginBO.getNickname();
        Integer deviceCode = registryLoginBO.getDeviceCode();

        return usersService.userRegistryOrLogin(mobile, code,nickname,deviceCode);
    }

    /**
     * 登出
     */
    @PostMapping("logout")
    public GraceJSONResult logout()  {

        // 删除redis中的Token就行
        redis.del(UserContext.getRedisTokenKey());
        return GraceJSONResult.ok();
    }
}
