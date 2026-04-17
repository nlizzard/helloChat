package com.nlizzard.tasks;

import com.nlizzard.utils.SMSUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SMSTask {

    private final SMSUtils smsUtils;

    @Async
    public void sendSMSInTask(String mobile, String code){
        // smsUtils.sendSMS(mobile, code); 调用腾讯云短信服务发送验证码
        log.info("异步任务中所发送的验证码为：{}", code);
    }

}
