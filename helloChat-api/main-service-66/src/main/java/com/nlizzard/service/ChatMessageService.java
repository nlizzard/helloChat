package com.nlizzard.service;

import com.nlizzard.pojo.netty.ChatMsg;

public interface ChatMessageService {

    /**
     * 保存聊天信息
     * @param chatMsg 聊天信息对象
     */
    void saveMsg(ChatMsg chatMsg);

}
