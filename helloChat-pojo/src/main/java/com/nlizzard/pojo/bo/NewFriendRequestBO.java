package com.nlizzard.pojo.bo;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class NewFriendRequestBO {

    private String myId;
    @NotBlank(message = "对方id不能为空")
    private String friendId;
    @NotBlank(message = "验证消息不能为空")
    private String verifyMessage;
    private String friendRemark;

}
