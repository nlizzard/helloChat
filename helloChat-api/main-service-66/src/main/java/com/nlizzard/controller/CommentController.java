package com.nlizzard.controller;

import com.nlizzard.base.BaseInfoProperties;
import com.nlizzard.grace.result.GraceJSONResult;
import com.nlizzard.pojo.bo.CommentBO;
import com.nlizzard.pojo.vo.CommentVO;
import com.nlizzard.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("comment")
@RequiredArgsConstructor
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
}
