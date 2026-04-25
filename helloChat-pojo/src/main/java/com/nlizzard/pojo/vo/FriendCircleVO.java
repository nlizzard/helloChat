package com.nlizzard.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nlizzard.pojo.FriendCircleLiked;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.nlizzard.utils.LocalDateUtils.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class FriendCircleVO implements Serializable {

    private String friendCircleId;
    private String userId;
    private String userNickname;
    private String userFace;
    private String words;
    private String images;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using= LocalDateTimeDeserializer.class)
    @JsonFormat(locale = LOCALE_ZH, timezone = TIMEZONE_GMT8, pattern = DATETIME_PATTERN)

    private LocalDateTime publishTime;

    // 点赞的朋友列表
    private List<FriendCircleLiked> likedFriends = new ArrayList<>();
    // 用于判断当前用户是否点赞过朋友圈
    private Boolean doILike = false;
    // 朋友圈的评论列表
    private List<CommentVO> commentList = new ArrayList<>();

}
