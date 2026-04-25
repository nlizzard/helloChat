package com.nlizzard.controller;

import com.nlizzard.base.BaseInfoProperties;
import com.nlizzard.grace.result.GraceJSONResult;
import com.nlizzard.pojo.bo.FriendCircleBO;
import com.nlizzard.service.FriendCircleService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("friendCircle")
@RequiredArgsConstructor
public class FriendCircleController extends BaseInfoProperties {


    private final FriendCircleService friendCircleService;


    /**
     * 发布朋友圈
     * @param friendCircleBO 朋友圈信息
     * @param request 请求对象
     * @return 结果
     */
    @PostMapping("publish")
    public GraceJSONResult publish(@RequestBody FriendCircleBO friendCircleBO,
                                   HttpServletRequest request) {

        String images = friendCircleBO.getImages();
        String words = friendCircleBO.getWords();
        String video = friendCircleBO.getVideo();

        if(StringUtils.isBlank(images) || StringUtils.isBlank(words) || StringUtils.isBlank(video)){
            return GraceJSONResult.errorMsg("朋友圈发布内容不能为空！");
        }

        String userId = request.getHeader(HEADER_USER_ID);

        friendCircleBO.setUserId(userId);
        friendCircleBO.setPublishTime(LocalDateTime.now());

        friendCircleService.publish(friendCircleBO);

        return GraceJSONResult.ok();
    }
}
