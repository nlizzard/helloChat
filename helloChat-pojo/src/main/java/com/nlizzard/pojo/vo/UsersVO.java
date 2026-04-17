package com.nlizzard.pojo.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.nlizzard.utils.LocalDateUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UsersVO {

    private String id;
    private String wechatNum;
    private String wechatNumImg;
    private String mobile;
    private String nickname;
    private String realName;
    private Integer sex;
    private String face;
    private String email;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @JsonSerialize(using = LocalDateSerializer.class)
    @JsonFormat(pattern = LocalDateUtils.DATE_PATTERN, timezone = LocalDateUtils.TIMEZONE_GMT8)
    private LocalDate birthday;

    private String country;
    private String province;
    private String city;
    private String district;
    private String chatBg;
    private String friendCircleBg;
    private String signature;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = LocalDateUtils.DATETIME_PATTERN, timezone = LocalDateUtils.TIMEZONE_GMT8)
    private LocalDateTime createdTime;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonFormat(pattern = LocalDateUtils.DATETIME_PATTERN, timezone = LocalDateUtils.TIMEZONE_GMT8)
    private LocalDateTime updatedTime;

    private String userToken;       // 用户会话令牌token，传递给前端让前端保存处理
}
