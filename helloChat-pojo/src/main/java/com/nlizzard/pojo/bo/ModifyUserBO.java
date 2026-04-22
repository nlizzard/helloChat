package com.nlizzard.pojo.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

import static com.nlizzard.utils.LocalDateUtils.*;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ModifyUserBO {

    @NotBlank(message = "用户id不能为空")
    private String userId;

    private String face;
    private Integer sex;
    private String nickname;
    private String wechatNum;

    private String province;
    private String city;
    private String district;
    private String chatBg;
    private String friendCircleBg;
    private String signature;

    @JsonFormat(locale = LOCALE_ZH, timezone = TIMEZONE_GMT8, pattern = DATE_PATTERN)
    private LocalDate birthday;

    @Email
    private String email;

    @JsonFormat(locale = LOCALE_ZH, timezone = TIMEZONE_GMT8, pattern = DATE_PATTERN)
    private LocalDate startWorkDate;

}

