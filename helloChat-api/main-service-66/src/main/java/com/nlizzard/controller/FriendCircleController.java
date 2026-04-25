package com.nlizzard.controller;

import com.nlizzard.base.BaseInfoProperties;
import com.nlizzard.grace.result.GraceJSONResult;
import com.nlizzard.pojo.FriendCircleLiked;
import com.nlizzard.pojo.bo.FriendCircleBO;
import com.nlizzard.pojo.vo.CommentVO;
import com.nlizzard.pojo.vo.FriendCircleVO;
import com.nlizzard.service.CommentService;
import com.nlizzard.service.FriendCircleService;
import com.nlizzard.utils.PagedGridResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
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

    private final CommentService commentService;


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
    public GraceJSONResult queryFriendCircleList(HttpServletRequest request,
                                   @RequestParam(defaultValue = "1", name = "page") Integer page,
                                   @RequestParam(defaultValue = "10", name = "pageSize") Integer pageSize) {

        String userId = request.getHeader(HEADER_USER_ID);

        PagedGridResult gridResult = friendCircleService.queryList(userId, page, pageSize);

        // 查询朋友圈的点赞列表、评论列表，以及当前用户是否点赞过朋友圈
        List<FriendCircleVO> list = (List<FriendCircleVO>)gridResult.getRows();
        // 遍历朋友圈列表，查询点赞列表、评论列表，以及当前用户是否点赞过朋友圈
        for (FriendCircleVO f : list) {
            // 查询朋友圈的点赞列表,塞到VO对象中
            String friendCircleId = f.getFriendCircleId();
            List<FriendCircleLiked> likedList = friendCircleService.queryLikedFriends(friendCircleId);
            f.setLikedFriends(likedList);

            // 判断当前用户是否点赞过朋友圈
            boolean res = friendCircleService.isLike(friendCircleId, userId);
            f.setDoILike(res);

            List<CommentVO> commentList = commentService.queryAll(friendCircleId);
            f.setCommentList(commentList);
        }
        return GraceJSONResult.ok(gridResult);
    }

    /**
     * 点赞朋友圈
     * @param friendCircleId 朋友圈ID
     * @param request 请求对象
     * @return 结果
     */
    @PostMapping("like")
    public GraceJSONResult like(String friendCircleId,
                                HttpServletRequest request) {

        String userId = request.getHeader(HEADER_USER_ID);
        friendCircleService.toggleLike(friendCircleId, userId,"like");

        return GraceJSONResult.ok();
    }

    /**
     * 取消点赞朋友圈
     * @param friendCircleId 朋友圈ID
     * @param request 请求对象
     * @return 结果
     */
    @PostMapping("unlike")
    public GraceJSONResult unlike(String friendCircleId,
                                  HttpServletRequest request) {

        String userId = request.getHeader(HEADER_USER_ID);
        friendCircleService.toggleLike(friendCircleId, userId,"unlike");

        return GraceJSONResult.ok();
    }

    /**
     * 查询朋友圈的点赞列表
     * @param friendCircleId 朋友圈ID
     * @return 结果
     */
    @PostMapping("likedFriends")
    public GraceJSONResult likedFriends(String friendCircleId) {
        List<FriendCircleLiked> likedList =
                friendCircleService.queryLikedFriends(friendCircleId);
        return GraceJSONResult.ok(likedList);
    }

    /**
     * 删除朋友圈
     * @param friendCircleId 朋友圈ID
     * @param request 请求对象
     * @return 结果
     */
    @PostMapping("delete")
    public GraceJSONResult delete(@NotBlank(message = "朋友圈ID不能为空") String friendCircleId,
                                  HttpServletRequest request) {

        String userId = request.getHeader(HEADER_USER_ID);
        friendCircleService.delete(friendCircleId, userId);

        return GraceJSONResult.ok();
    }
}
