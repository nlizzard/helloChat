package com.nlizzard.enums;

/**
 * Netty 发送消息的类型/动作 枚举
 */
public enum LoginDeviceTypeEnum {

    PC_COMPUTER(1,  "PC端"),
    MOBILE(2, "移动端"),
    TABLET(3, "平板");

    public final Integer deviceCode;
    public final String type;

    LoginDeviceTypeEnum(Integer deviceCode, String type){
        this.deviceCode = deviceCode;
        this.type = type;
    }

    LoginDeviceTypeEnum getLoginDeviceTypeEnumByCode(Integer code){
        for (LoginDeviceTypeEnum loginDeviceTypeEnum : LoginDeviceTypeEnum.values()) {
            if (loginDeviceTypeEnum.deviceCode.equals(code)) {
                return loginDeviceTypeEnum;
            }
        }
        return null;
    }
}

