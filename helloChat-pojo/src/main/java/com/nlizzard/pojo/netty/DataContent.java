package com.nlizzard.pojo.netty;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DataContent {

    // 动作类型
    private Integer action;

    // 用户的聊天内容entity
    private ChatMsg chatMsg;

    // 格式化后的聊天时间
    private String chatTime;

    // 扩展字段
    private String extend;

    // Netty服务器节点信息
    private NettyServerNode serverNode;

}
