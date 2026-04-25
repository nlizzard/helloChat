package com.nlizzard.pojo;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@ToString
public class Comment implements Serializable {

    private String id;

    /**
     * 评论的朋友圈是哪个用户的关联id
     */
    private String belongUserId;

    /**
     * 如果是回复留言，则本条为子留言，需要关联查询
     */
    private String fatherId;

    /**
     * 评论的那个朋友圈的主键id
     */
    private String friendCircleId;

    /**
     * 发布留言的用户id
     */
    private String commentUserId;

    /**
     * 留言内容
     */
    private String commentContent;

    /**
     * 留言时间
     */
    private LocalDateTime createdTime;
}
