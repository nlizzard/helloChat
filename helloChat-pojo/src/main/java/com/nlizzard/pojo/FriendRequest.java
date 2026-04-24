package com.nlizzard.pojo;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@ToString
public class FriendRequest implements Serializable {


    private String id;

    /**
     * 添加好友，发起请求的用户id
     */
    private String myId;

    /**
     * 要添加的朋友的id
     */
    private String friendId;

    /**
     * 好友的备注名
     */
    private String friendRemark;

    /**
     * 请求的留言，验证消息
     */
    private String verifyMessage;

    /**
     * 请求被好友审核的状态，0-待审核；1-已添加，2-已过期
     */
    private Integer verifyStatus;

    /**
     * 发起请求的时间
     */
    private LocalDateTime requestTime;
}
