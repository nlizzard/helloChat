package com.nlizzard.controller;

import com.nlizzard.base.BaseInfoProperties;
import com.nlizzard.grace.result.GraceJSONResult;
import com.nlizzard.pojo.bo.CommentBO;
import com.nlizzard.pojo.vo.CommentVO;
import com.nlizzard.service.CommentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("comment")
@RequiredArgsConstructor
@Validated
public class CommentController extends BaseInfoProperties {

    private final CommentService commentService;

    /**
     * 发表朋友圈评论
     * @param friendCircleBO 评论信息
     * @return 评论信息
     */
    @PostMapping("create")
    public GraceJSONResult create(@RequestBody @Valid CommentBO friendCircleBO) {
        CommentVO commentVO = commentService.createComment(friendCircleBO);
        return GraceJSONResult.ok(commentVO);
    }

    /**
    * 查询朋友圈的评论列表
    * @param friendCircleId 朋友圈ID
    * @return 评论列表
    */
    @PostMapping("query")
    public GraceJSONResult query(@NotBlank(message = "朋友圈ID不能为空") String friendCircleId) {
        return GraceJSONResult.ok(commentService.queryAll(friendCircleId));
    }

    /**
     * 删除朋友圈评论
     * @param commentUserId 评论用户ID
     * @param commentId 评论ID
     * @param friendCircleId 朋友圈ID
     * @return 结果
     */
    @PostMapping("delete")
    public GraceJSONResult delete(@NotBlank(message = "发表评论的用户ID不能为空") String commentUserId,
                                  @NotBlank(message = "评论ID不能为空")String commentId,
                                  @NotBlank(message = "朋友圈ID不能为空")String friendCircleId) {


        commentService.deleteComment(commentUserId, commentId, friendCircleId);
        return GraceJSONResult.ok();
    }
}
