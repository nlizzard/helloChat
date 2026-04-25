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
public class CommentBO {

    @NotBlank(message = "用户id不能为空")
    private String belongUserId;
    @NotBlank(message = "朋友圈id不能为空")
    private String friendCircleId;

    private String fatherId;

    @NotBlank(message = "评论用户id不能为空")
    private String commentUserId;
    @NotBlank(message = "评论内容不能为空")
    private String commentContent;
}

