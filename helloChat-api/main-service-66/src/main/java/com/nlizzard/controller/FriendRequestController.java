package com.nlizzard.controller;

import com.nlizzard.base.BaseInfoProperties;
import com.nlizzard.grace.result.GraceJSONResult;
import com.nlizzard.pojo.bo.NewFriendRequestBO;
import com.nlizzard.service.FriendRequestService;
import com.nlizzard.utils.PagedGridResult;
import com.nlizzard.utils.UserContext;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("friendRequest")
@Slf4j
@RequiredArgsConstructor
@Validated
public class FriendRequestController extends BaseInfoProperties {


    private final FriendRequestService friendRequestService;

    /**
     * 发送添加好友请求接口
     * @param friendRequestBO 添加好友请求BO对象，包含接收者ID、备注等属性
     * @return GraceJSONResult对象，表示操作结果
     */
    @PostMapping("add")
    public GraceJSONResult add(@RequestBody @Valid NewFriendRequestBO friendRequestBO) {
        friendRequestBO.setMyId(UserContext.getUserId());
        // 发送添加好友请求
        friendRequestService.addNewRequest(friendRequestBO);
        return GraceJSONResult.ok();
    }


    /**
     * 查询好友请求列表接口
     * @param page 分页页码，默认为1
     * @param pageSize 每页记录数，默认为10
     * @return GraceJSONResult对象，包含分页结果的好友请求列表
     */
    @PostMapping("queryNew")
    public GraceJSONResult queryNew(@RequestParam(defaultValue = "1", name = "page") Integer page,
                                    @RequestParam(defaultValue = "10", name = "pageSize") Integer pageSize) {

        String userId = UserContext.getUserId();

        PagedGridResult result = friendRequestService.queryNewFriendList(userId,
                page,
                pageSize);

        return GraceJSONResult.ok(result);
    }

    /**
     * 通过好友请求接口
     * @param friendRequestId 好友请求ID，唯一标识一个好友请求记录
     * @param friendRemark 好友备注信息，可以为空
     * @return GraceJSONResult对象，表示操作结果
     */
    @PostMapping("pass")
    public GraceJSONResult pass(@NotBlank(message = "请求记录ID不能为空") String friendRequestId, String friendRemark) {
        friendRequestService.passNewFriend(friendRequestId, friendRemark);
        return GraceJSONResult.ok();
    }
}
