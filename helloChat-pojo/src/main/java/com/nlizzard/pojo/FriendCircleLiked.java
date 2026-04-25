package com.nlizzard.pojo;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@ToString
public class FriendCircleLiked implements Serializable {


    private String id;

    /**
     * 朋友圈归属用户的id
     */
    private String belongUserId;

    /**
     * 点赞的那个朋友圈id
     */
    private String friendCircleId;

    /**
     * 点赞的那个用户id
     */
    private String likedUserId;

    /**
     * 点赞用户的昵称
     */
    private String likedUserName;

    /**
     * 点赞时间
     */
    private LocalDateTime createdTime;
}
