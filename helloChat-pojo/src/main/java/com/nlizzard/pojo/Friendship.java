package com.nlizzard.pojo;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@ToString
public class Friendship implements Serializable {


    private String id;

    /**
     * 自己的用户id
     */
    private String myId;

    /**
     * 我朋友的id
     */
    private String friendId;

    /**
     * 好友的备注名
     */
    private String friendRemark;

    /**
     * 聊天背景，局部
     */
    private String chatBg;

    /**
     * 是否消息免打扰，0-打扰，不忽略消息(默认)；1-免打扰，忽略消息
     */
    private Integer isMsgIgnore;

    /**
     * 是否拉黑，0-好友(默认)；1-已拉黑
     */
    private Integer isBlack;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
