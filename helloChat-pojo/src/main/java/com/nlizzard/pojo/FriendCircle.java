package com.nlizzard.pojo;


import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@ToString
public class FriendCircle implements Serializable {


    private String id;

    /**
     * 哪个用户发的朋友圈，用户id
     */
    private String userId;

    /**
     * 文字内容
     */
    private String words;

    /**
     * 图片内容，url用逗号分割
     */
    private String images;

    /**
     * 单个视频的url
     */
    private String video;

    /**
     * 发布朋友圈的时间
     */
    private LocalDateTime publishTime;
}