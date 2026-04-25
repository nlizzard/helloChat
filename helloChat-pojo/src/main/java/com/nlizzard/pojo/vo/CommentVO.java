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

import java.time.LocalDateTime;

import static com.nlizzard.utils.LocalDateUtils.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CommentVO {
    private String commentId;
    private String belongUserId;
    private String friendCircleId;

    private String fatherId;
    private String commentUserId;
    private String commentUserNickname;
    private String commentUserFace;
    private String commentContent;

    private String replyedUserNickname;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using= LocalDateTimeDeserializer.class)
    @JsonFormat(locale = LOCALE_ZH, timezone = TIMEZONE_GMT8, pattern = DATETIME_PATTERN)
    private LocalDateTime createdTime;
}
