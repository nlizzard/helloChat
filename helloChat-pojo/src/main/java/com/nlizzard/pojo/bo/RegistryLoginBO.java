package com.nlizzard.pojo.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RegistryLoginBO {

    @NotBlank(message = "手机号不能为空")
    @Length(min = 11, max = 11, message = "手机号长度不正确")
    private String mobile;

    @NotBlank(message = "验证码不能为空")
    private String smsCode;

    private String nickname;

    @NotBlank(message = "设备类型码不能为空")
    private Integer deviceCode;
}
