package com.nlizzard.controller;

import com.nlizzard.base.BaseInfoProperties;
import com.nlizzard.grace.result.GraceJSONResult;
import com.nlizzard.pojo.bo.FriendCircleBO;
import com.nlizzard.service.FriendCircleService;
import com.nlizzard.utils.PagedGridResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

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

    /**
     * 查询朋友圈列表
     * @param request 请求对象
     * @param page 页码
     * @param pageSize 每页条数
     * @return 结果
     */
    @PostMapping("queryList")
    public GraceJSONResult publish(HttpServletRequest request,
                                   @RequestParam(defaultValue = "1", name = "page") Integer page,
                                   @RequestParam(defaultValue = "10", name = "pageSize") Integer pageSize) {

        String userId = request.getHeader(HEADER_USER_ID);

        PagedGridResult gridResult = friendCircleService.queryList(userId, page, pageSize);

        return GraceJSONResult.ok(gridResult);
    }
}
