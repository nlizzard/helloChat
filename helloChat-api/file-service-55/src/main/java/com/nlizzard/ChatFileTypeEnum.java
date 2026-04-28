package com.nlizzard;

/**
 * 文件类型枚举
 */
public enum ChatFileTypeEnum {
    IMAGE(1, "photo","图片"),
    VOICE(2, "voice","语音"),
    VIDEO(3, "video","视频"),
    CHAT_BG(4, "chatBg","聊天背景");

    public final Integer type;
    public final String path;
    public final String content;

    ChatFileTypeEnum(Integer type, String path, String content){
        this.type = type;
        this.path = path;
        this.content = content;
    }
}
