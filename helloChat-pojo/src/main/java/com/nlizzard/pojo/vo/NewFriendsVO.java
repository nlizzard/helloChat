package com.nlizzard.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
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

import static com.nlizzard.utils.LocalDateUtils.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class NewFriendsVO implements Serializable {

    private String friendRequestId;
    private String myFriendId;
    private String myFriendFace;
    private String myFriendNickname;
    private String verifyMessage;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using= LocalDateTimeDeserializer.class)
    @JsonFormat(locale = LOCALE_ZH, timezone = TIMEZONE_GMT8, pattern = DATETIME_PATTERN_4)
    private LocalDateTime requestTime;
    private Integer verifyStatus;

    // 预留前端使用，是否被点击过了，默认false
    private boolean isTouched = false;

}
