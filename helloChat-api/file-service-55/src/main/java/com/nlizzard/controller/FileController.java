package com.nlizzard.controller;

import com.nlizzard.enums.ChatFileTypeEnum;
import com.nlizzard.api.feign.UserInfoMicroServiceFeign;
import com.nlizzard.base.BaseInfoProperties;
import com.nlizzard.config.MinIOConfig;
import com.nlizzard.exceptions.GraceException;
import com.nlizzard.grace.result.GraceJSONResult;
import com.nlizzard.grace.result.ResponseStatusEnum;
import com.nlizzard.pojo.vo.UsersVO;
import com.nlizzard.pojo.vo.VideoMsgVO;
import com.nlizzard.utils.*;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.nlizzard.grace.result.ResponseStatusEnum.FILE_UPLOAD_FAILD;

@RestController
@RequestMapping("file")
@RequiredArgsConstructor
public class FileController extends BaseInfoProperties {

    private final MinIOConfig minIOConfig;

    private final UserInfoMicroServiceFeign userInfoMicroServiceFeign;

    // springboot文件上传实现方案一（传统单体项目可使用）
//    @PostMapping("uploadFace1")
//    public GraceJSONResult uploadFace1(HttpServletRequest request,@RequestParam("file") MultipartFile file) throws Exception {
//
//        String userId = request.getHeader(HEADER_USER_ID);
//
//        // abc.123.456.png
//        String filename = file.getOriginalFilename();   // 获得文件原始名称
//        String suffixName = null;  // 从最后一个.开始截取
//        if (filename != null) {
//            suffixName = filename.substring(filename.lastIndexOf("."));
//        }
//        if(StringUtils.isBlank(suffixName)){
//            return GraceJSONResult.errorMsg("文件格式不正确");
//        }
//
//        String newFileName = userId + suffixName;   // 文件的新名称
//
//        // 设置文件存储路径，可以存放到任意的指定路径
//        String rootPath = "D:\\program" + File.separator; // 上传文件的存放位置
//
//        String filePath = rootPath + File.separator + "face" + File.separator + newFileName;
//        File newFile = new File(filePath);
//        // 判断目标文件所在目录是否存在
//        if (!newFile.getParentFile().exists()) {
//            // 如果目标文件所在目录不存在，则创建父级目录
//            newFile.getParentFile().mkdirs();
//        }
//
//        // 将内存中的数据写入磁盘
//        file.transferTo(newFile);
//
//        return GraceJSONResult.ok();
//    }


    // 分布式存储技术方案minIO实现文件上传（微服务项目推荐使用）
    @PostMapping("uploadFace")
    public GraceJSONResult uploadFace(@RequestParam("file") MultipartFile file) throws Exception {
        String userId = UserContext.getUserId();

        String filename = file.getOriginalFilename();   // 获得文件原始名称
        if (StringUtils.isBlank(filename)) {
            return GraceJSONResult.errorCustom(FILE_UPLOAD_FAILD);
        }
        // 定义存放路径，face/userId/filename
        filename = "face" + "/" + userId + "/" + filename;

        String faceUrl = MinIOUtils.uploadFile(minIOConfig.getBucketName(),
                filename,
                file.getInputStream(),true);
        /*
          微服务远程调用更新用户头像到数据库 OpenFeign
          如果前端没有保存按钮则可以这么做，如果有保存提交按钮，则在前端可以触发
          此处则不需要进行微服务调用，让前端触发保存提交到后台进行保存
         */

        GraceJSONResult jsonResult = userInfoMicroServiceFeign.updateFace(userId, faceUrl);
        Object data = jsonResult.getData();

        String json = JsonUtils.objectToJson(data);
        UsersVO usersVO = JsonUtils.jsonToPojo(json, UsersVO.class);

        return GraceJSONResult.ok(usersVO);
    }

    /**
     * 生成微信二维码，并上传到minIO中
     * @param wechatNumber 微信号
     * @return 微信二维码存放路径
     */
    @PostMapping("generatorQrCode")
    public String generatorQrCode(@RequestParam("wechatNumber")String wechatNumber) throws Exception {

        String userId = UserContext.getUserId();
        // 构建map对象
        Map<String, String> map = new HashMap<>();
        map.put("wechatNumber", wechatNumber);
        map.put("userId", userId);

        // 把对象转换为json字符串，用于存储到二维码中
        String data = JsonUtils.objectToJson(map);

        // 生成二维码
        InputStream qrCode = QrCodeUtils.generateQRCodeInputStream(data);

        // 把二维码上传到minio中
        String uuid = UUID.randomUUID().toString();
        String objectName = "wechatNumber" + "/" + userId + "/" + uuid + ".png";
        return MinIOUtils.uploadFile(minIOConfig.getBucketName(), objectName, qrCode,true);
    }

    /**
     * 上传朋友圈背景图接口
     * @param file 朋友圈背景图文件
     * @return 朋友圈背景图URL地址
     */
    @PostMapping("uploadFriendCircleBg")
    public GraceJSONResult uploadFriendCircleBg(@RequestParam("file") MultipartFile file) throws Exception {

        String userId = UserContext.getUserId();
        // 获得文件原始名称
        String filename = file.getOriginalFilename();
        if (StringUtils.isBlank(filename)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }

        filename = "friendCircleBg"
                + "/" + userId
                + "/" + dealWithoutFilename(filename);

        String imageUrl = MinIOUtils.uploadFile(minIOConfig.getBucketName(),
                filename,
                file.getInputStream(),
                true);

        GraceJSONResult jsonResult = userInfoMicroServiceFeign
                .updateFriendCircleBg(userId, imageUrl);
        Object data = jsonResult.getData();

        String json = JsonUtils.objectToJson(data);
        UsersVO usersVO = JsonUtils.jsonToPojo(json, UsersVO.class);

        return GraceJSONResult.ok(usersVO);
    }

    /**
     * 上传聊天背景图接口
     * @param file 聊天背景图文件
     * @return 聊天背景图URL地址
     */
    @PostMapping("uploadChatBg")
    public GraceJSONResult uploadChatBg(@RequestParam("file") MultipartFile file) throws Exception {

        String userId = UserContext.getUserId();

        String chatImageUrl = uploadForChatFiles(file,userId, ChatFileTypeEnum.CHAT_BG);

        // 微服务远程调用更新用户聊天背景图到数据库 OpenFeign
        GraceJSONResult jsonResult = userInfoMicroServiceFeign
                .updateChatBg(userId, chatImageUrl);
        Object data = jsonResult.getData();

        String json = JsonUtils.objectToJson(data);
        UsersVO usersVO = JsonUtils.jsonToPojo(json, UsersVO.class);

        return GraceJSONResult.ok(usersVO);
    }

    /**
     * 上传朋友圈图片接口
     * @param file 朋友圈图片文件
     * @return 朋友圈图片URL地址
     */
    @PostMapping("uploadFriendCircleImage")
    public GraceJSONResult uploadFriendCircleImage(@RequestParam("file") MultipartFile file) throws Exception {

        String userId = UserContext.getUserId();

        String filename = file.getOriginalFilename();   // 获得文件原始名称
        if (StringUtils.isBlank(filename)) {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }

        filename = "friendCircleImage"
                + "/" + userId
                + "/" + dealWithoutFilename(filename);

        String imageUrl = MinIOUtils.uploadFile(minIOConfig.getBucketName(),
                filename,
                file.getInputStream(),
                true);

        return GraceJSONResult.ok(imageUrl);
    }


    /**
     * 上传聊天图片消息
     * @param file 聊天图片文件
     * @return 聊天图片URL地址
     */
    @PostMapping("uploadChatPhoto")
    public GraceJSONResult uploadChatPhoto(@RequestParam("file") MultipartFile file) throws Exception {

        String userId = UserContext.getUserId();

        String imageUrl = uploadForChatFiles(file, userId, ChatFileTypeEnum.IMAGE);

        return GraceJSONResult.ok(imageUrl);
    }

    /**
     * 上传聊天视频消息
     * @param file 聊天视频文件
     * @return VideoMsgVO对象，包含视频URL地址和封面URL地址
     */
    @PostMapping("uploadChatVideo")
    public GraceJSONResult uploadChatVideo(@RequestParam("file") MultipartFile file) throws Exception {

        String userId = UserContext.getUserId();

        String videoUrl = uploadForChatFiles(file, userId, ChatFileTypeEnum.VIDEO);

        // 帧，封面获取 = 视频截帧 截取第一帧
        String coverName = UUID.randomUUID() + ".jpg";   // 视频封面的名称
        InputStream coverFile =  JcodecVideoUtil.fetchFrameInputStream(file);

        // 上传封面到minio
        String coverUrl = MinIOUtils.uploadFile(minIOConfig.getBucketName(),
                coverName,
                coverFile,
                true);

        VideoMsgVO videoMsgVO = new VideoMsgVO();
        videoMsgVO.setVideoPath(videoUrl);
        videoMsgVO.setCover(coverUrl);

        return GraceJSONResult.ok(videoMsgVO);
    }

    /**
     * 上传聊天语音消息
     * @param file 聊天语音文件
     * @return 聊天语音URL地址
     */
    @PostMapping("uploadChatVoice")
    public GraceJSONResult uploadChatVoice(@RequestParam("file") MultipartFile file) throws Exception {
        String userId = UserContext.getUserId();

        String voiceUrl = uploadForChatFiles(file, userId, ChatFileTypeEnum.VOICE);

        return GraceJSONResult.ok(voiceUrl);
    }

    /**
     * 上传聊天文件到minIO（例如：聊天背景图、图片、音频、视频等）
     * @param file 聊天文件
     * @param userId 用户ID
     * @param fileType 文件类型枚举
     * @return 聊天文件URL地址
     * @throws Exception 文件上传异常
     */
    private String uploadForChatFiles(MultipartFile file,
                                      String userId,
                                      ChatFileTypeEnum fileType) throws Exception {

        String filename = file.getOriginalFilename();   // 获得文件原始名称
        if (org.apache.commons.lang3.StringUtils.isBlank(filename)) {
            GraceException.display(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }

        filename = "chat"
                + "/" + userId
                + "/" + fileType.path
                + "/" + dealWithoutFilename(filename);

        return MinIOUtils.uploadFile(minIOConfig.getBucketName(),
                filename,
                file.getInputStream(),
                true);
    }


    /**
     * 处理文件名称，生成新的文件名称，格式：原文件名称-uuid.后缀
     * @param filename 原文件名称
     * @return 新文件名称
     */
    private String dealWithFilename(String filename) {
        String suffixName = filename.substring(filename.lastIndexOf("."));
        String fName = filename.substring(0, filename.lastIndexOf("."));
        String uuid = UUID.randomUUID().toString();
        return fName + "-" + uuid + suffixName;
    }
    /**
     * 处理文件名称，生成新的文件名称，格式：uuid.后缀
     * @param filename 原文件名称
     * @return 新文件名称
     */
    private String dealWithoutFilename(String filename) {
        String suffixName = filename.substring(filename.lastIndexOf("."));
        String uuid = UUID.randomUUID().toString();
        return uuid + suffixName;
    }
}
