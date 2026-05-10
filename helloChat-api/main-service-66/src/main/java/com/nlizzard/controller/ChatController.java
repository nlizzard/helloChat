package com.nlizzard.controller;

import com.nlizzard.base.BaseInfoProperties;
import com.nlizzard.grace.result.GraceJSONResult;
import com.nlizzard.pojo.netty.NettyServerNode;
import com.nlizzard.service.ChatMessageService;
import com.nlizzard.utils.JsonUtils;
import com.nlizzard.utils.PagedGridResult;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("chat")
@Validated
@RequiredArgsConstructor
public class ChatController extends BaseInfoProperties {

    private final ChatMessageService chatMessageService;

    private final CuratorFramework zkClient;

    /**
     * 获取我的未读消息数量
     */
    @PostMapping("getMyUnReadCounts")
    public GraceJSONResult getMyUnReadCounts(@NotBlank(message="用户id不能为空") String myId) {
        Map<Object, Object> map = redis.hgetall(CHAT_MSG_LIST + ":" + myId);
        return GraceJSONResult.ok(map);
    }

    /**
     * 清空我的未读消息数量
     */
    @PostMapping("clearMyUnReadCounts")
    public GraceJSONResult clearMyUnReadCounts(@NotBlank(message="用户id不能为空") String myId,
                                               @NotBlank(message="消息发送方的id不能为空") String oppositeId) {
        redis.setHashValue(CHAT_MSG_LIST + ":" + myId, oppositeId, "0");
        return GraceJSONResult.ok();
    }

    /**
     * 获取我的聊天消息列表
     */
    @PostMapping("list/{senderId}/{receiverId}")
    public GraceJSONResult list(@PathVariable @NotBlank(message="发送方id不能为空") String senderId,
                                @PathVariable @NotBlank(message="接收方id不能为空") String receiverId,
                                Integer page,
                                Integer pageSize) {

        if (page == null) page = 1;
        if (pageSize == null) pageSize = 20;

        PagedGridResult gridResult = chatMessageService.queryChatMsgList(
                senderId,
                receiverId,
                page,
                pageSize);
        return GraceJSONResult.ok(gridResult);
    }

    // 修改语音消息为已读
    @PostMapping("signRead/{msgId}")
    public GraceJSONResult signRead(@PathVariable @NotBlank(message = "语音消息id不能为空") String msgId) {
        chatMessageService.updateMsgSignRead(msgId);
        return GraceJSONResult.ok();
    }

    // 获取netty服务器ip信息
    @PostMapping("getNettyOnlineInfo")
    public GraceJSONResult getNettyOnlineInfo() throws Exception {

        // 从zookeeper中获得当前已经注册的netty 服务列表
        String path = "/netty_server_list";
        List<String> list = zkClient.getChildren().forPath(path);

        List<NettyServerNode> serverNodeList = new ArrayList<>();
        for (String node:list) {
            String nodeValue = new String(zkClient.getData().forPath(path + "/" + node));

            NettyServerNode serverNode = JsonUtils.jsonToPojo(nodeValue, NettyServerNode.class);
            serverNodeList.add(serverNode);
        }

        // 计算当前哪个zk的node是最少连接，获得[ip:port]并且返回给前端
        NettyServerNode minNode = serverNodeList
                .stream()
                .min(Comparator.comparing(NettyServerNode::getOnlineCounts))
                .orElseThrow(()-> new RuntimeException("无法找到最少连接的Netty服务器节点"));

        return GraceJSONResult.ok(minNode);
    }
}
