package com.nlizzard.utils;

import com.nlizzard.config.TencentCloudProperties;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.sms.v20210111.SmsClient;
import com.tencentcloudapi.sms.v20210111.models.SendSmsRequest;
import com.tencentcloudapi.sms.v20210111.models.SendSmsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 腾讯短信服务工具类
 */
@Component
@RequiredArgsConstructor
public class SMSUtils {

    private final TencentCloudProperties tencentCloudProperties;

    public void sendSMS(String phone, String code) throws Exception {

        try {
            // 实例化一个认证对象，入参需要传入腾讯云账户 SecretId，SecretKey。
            // 为了保护密钥安全，建议将密钥设置在环境变量中或者配置文件中，请参考本文凭证管理章节。
            // 硬编码密钥到代码中有可能随代码泄露而暴露，有安全隐患，并不推荐。
            // Credential cred = new Credential("SecretId", "SecretKey");
            Credential cred = new Credential(tencentCloudProperties.getSecretId(),
                    tencentCloudProperties.getSecretKey());

            // 实例化一个http选项，可选的，没有特殊需求可以跳过
            HttpProfile httpProfile = new HttpProfile();

//            httpProfile.setReqMethod("POST"); // 默认使用POST

            /* SDK会自动指定域名。通常是不需要特地指定域名的，但是如果你访问的是金融区的服务
             * 则必须手动指定域名，例如sms的上海金融区域名： sms.ap-shanghai-fsi.tencentcloudapi.com */
            httpProfile.setEndpoint("sms.tencentcloudapi.com");

            // 实例化一个client选项
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            SmsClient client = new SmsClient(cred, "ap-nanjing", clientProfile);

            // 实例化一个请求对象,每个接口都会对应一个request对象
            SendSmsRequest req = new SendSmsRequest();
            String[] phoneNumberSet1 = {"+86" + phone};//电话号码
            req.setPhoneNumberSet(phoneNumberSet1);
            req.setSmsSdkAppId("1400568450");   // 短信应用ID: 短信SdkAppId在 [短信控制台] 添加应用后生成的实际SdkAppId
            req.setSignName("风间影月");         // 签名
            req.setTemplateId("1108902");       // 模板id：必须填写已审核通过的模板 ID。模板ID可登录 [短信控制台] 查看

            /* 模板参数（自定义占位变量）: 若无模板参数，则设置为空 */
            String[] templateParamSet1 = {code};
            req.setTemplateParamSet(templateParamSet1);

            // 返回的resp是一个SendSmsResponse的实例，与请求对象对应
            SendSmsResponse resp = client.SendSms(req);
            // 输出json格式的字符串回包
            //System.out.println(SendSmsResponse.toJsonString(resp));
        } catch (TencentCloudSDKException e) {
            System.out.println(e.toString());
        }
    }

}


