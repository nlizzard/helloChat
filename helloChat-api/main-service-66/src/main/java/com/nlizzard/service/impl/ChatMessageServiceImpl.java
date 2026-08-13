package com.nlizzard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nlizzard.base.BaseInfoProperties;
import com.nlizzard.exceptions.GraceException;
import com.nlizzard.grace.result.ResponseStatusEnum;
import com.nlizzard.mapper.ChatMessageMapper;
import com.nlizzard.mapper.FriendshipMapper;
import com.nlizzard.pojo.ChatMessage;
import com.nlizzard.pojo.Friendship;
import com.nlizzard.pojo.netty.ChatMsg;
import com.nlizzard.service.ChatMessageService;
import com.nlizzard.utils.PagedGridResult;
import com.nlizzard.utils.UserContext;
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

    private final FriendshipMapper friendshipMapper;

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
        String myId = UserContext.getUserId();

        // 只能查询与我相关的聊天消息列表
        if (!senderId.equals(myId) && !receiverId.equals(myId)) {
            GraceException.display(ResponseStatusEnum.NO_AUTH);
        }

        // 是否存在好友关系
        LambdaQueryWrapper<Friendship> friendShipQueryWrapper = new LambdaQueryWrapper<Friendship>()
                .or(qw -> qw.eq(Friendship::getMyId, senderId)
                        .eq(Friendship::getFriendId, receiverId))
                .or(qw -> qw.eq(Friendship::getMyId, receiverId)
                        .eq(Friendship::getFriendId, senderId));
        // 好友关系是双向存储（A->B 与 B->A 各存一行），这里用 selectCount 判断是否存在好友关系即可。
        // 不能用 selectOne：OR 条件会同时命中两行，结果 > 1 时会抛 TooManyResultsException。
        long friendCount = friendshipMapper.selectCount(friendShipQueryWrapper);
        if (friendCount <= 0) {
            GraceException.display(ResponseStatusEnum.NO_AUTH);
        }

        Page<ChatMessage> pageInfo = new Page<>(page, pageSize);

        LambdaQueryWrapper<ChatMessage> queryWrapper = new LambdaQueryWrapper<ChatMessage>()
                .or(qw -> qw.eq(ChatMessage::getSenderId, senderId)
                        .eq(ChatMessage::getReceiverId, receiverId))
                .or(qw -> qw.eq(ChatMessage::getSenderId, receiverId)
                        .eq(ChatMessage::getReceiverId, senderId))
                .orderByDesc(ChatMessage::getChatTime);

        chatMessageMapper.selectPage(pageInfo, queryWrapper);

        // 获得列表后，倒着排序，因为聊天记录是展现最新的数据在聊天框的最下方，旧的数据在上方
        // 逆向降序的处理
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

        String myId = UserContext.getUserId();
        ChatMessage chatMessage = chatMessageMapper.selectById(msgId);
        // 消息不存在，或者消息的接受者不是当前用户，都不能修改为已读
        if(chatMessage == null || !chatMessage.getReceiverId().equals(myId)) {
            GraceException.display(ResponseStatusEnum.FAILED);
        }

        ChatMessage message = new ChatMessage();
        message.setId(msgId);
        message.setIsRead(true);

        chatMessageMapper.updateById(message);
    }
}
