package com.nlizzard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nlizzard.base.BaseInfoProperties;
import com.nlizzard.mapper.ChatMessageMapper;
import com.nlizzard.pojo.ChatMessage;
import com.nlizzard.pojo.netty.ChatMsg;
import com.nlizzard.service.ChatMessageService;
import com.nlizzard.utils.PagedGridResult;
import kotlin.jvm.internal.Lambda;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl extends BaseInfoProperties implements ChatMessageService {

    private final ChatMessageMapper chatMessageMapper;

    // 保存聊天消息到数据库中
    @Transactional
    @Override
    public void saveMsg(ChatMsg chatMsg) {

        ChatMessage message = new ChatMessage();
        BeanUtils.copyProperties(chatMsg, message);

        // 手动设置聊天信息的主键id
        message.setId(chatMsg.getMsgId());

        chatMessageMapper.insert(message);

        // 记录未读消息数量，让前端消息界面可以显示未读消息的数量
        String receiverId = chatMsg.getReceiverId();
        String senderId = chatMsg.getSenderId();
        // 通过redis累加信息接受者的对应记录
        redis.incrementHash(CHAT_MSG_LIST + ":" + receiverId, senderId, 1);

    }

    // 查询聊天消息列表
    @Override
    public PagedGridResult queryChatMsgList(String senderId,
                                            String receiverId,
                                            Integer page,
                                            Integer pageSize) {

        Page<ChatMessage> pageInfo = new Page<>(page, pageSize);

        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<ChatMessage>()
                .or(qw -> qw.eq(ChatMessage::getSenderId, senderId)
                        .eq(ChatMessage::getReceiverId, receiverId))
                .or(qw -> qw.eq(ChatMessage::getSenderId, receiverId)
                        .eq(ChatMessage::getReceiverId, senderId))
                .orderByDesc(ChatMessage::getChatTime);

        chatMessageMapper.selectPage(pageInfo, queryWrapper);

        // 获得列表后，倒着排序，因为聊天记录是展现最新的数据在聊天框的最下方，旧的数据在上方
        // 逆向逆序的处理
        List<ChatMessage> list = pageInfo.getRecords();
        List<ChatMessage> msgList  = list.stream().sorted(
                Comparator.comparing(ChatMessage::getChatTime)
        ).collect(Collectors.toList());

        pageInfo.setRecords(msgList);

        return setterPagedGridPlus(pageInfo);
    }

    // 修改语音消息为已读
    @Transactional
    @Override
    public void updateMsgSignRead(String msgId) {

        ChatMessage message = new ChatMessage();
        message.setId(msgId);
        message.setIsRead(true);

        chatMessageMapper.updateById(message);
    }
}
