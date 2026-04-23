package com.nlizzard.api.feign;


import com.nlizzard.grace.result.GraceJSONResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "main-service")
public interface UserInfoMicroServiceFeign {

    @PostMapping("/userInfo/updateFace")
    GraceJSONResult updateFace(@RequestParam("userId") String userId,
                               @RequestParam("faceUrl") String faceUrl);

    @PostMapping("/userInfo/updateFriendCircleBg")
    GraceJSONResult updateFriendCircleBg(
            @RequestParam("userId") String userId,
            @RequestParam("friendCircleBg") String friendCircleBg);
}
