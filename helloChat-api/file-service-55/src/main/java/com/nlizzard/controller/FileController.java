package com.nlizzard.controller;

import com.nlizzard.config.MinIOConfig;
import com.nlizzard.grace.result.GraceJSONResult;
import com.nlizzard.grace.result.ResponseStatusEnum;
import com.nlizzard.utils.MinIOUtils;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

import static com.nlizzard.grace.result.ResponseStatusEnum.FILE_UPLOAD_FAILD;

@RestController
@RequestMapping("file")
@Validated
@RequiredArgsConstructor
public class FileController {

    private final MinIOConfig minIOConfig;

    // springboot文件上传实现方案一（传统单体项目可使用）
    @PostMapping("uploadFace1")
    public GraceJSONResult uploadFace1(@RequestParam("file") MultipartFile file,
                                       @NotNull(message = "用户ID不能为空") String userId) throws Exception {

        // abc.123.456.png
        String filename = file.getOriginalFilename();   // 获得文件原始名称
        String suffixName = null;  // 从最后一个.开始截取
        if (filename != null) {
            suffixName = filename.substring(filename.lastIndexOf("."));
        }
        if(StringUtils.isBlank(suffixName)){
            return GraceJSONResult.errorMsg("文件格式不正确");
        }

        String newFileName = userId + suffixName;   // 文件的新名称

        // 设置文件存储路径，可以存放到任意的指定路径
        String rootPath = "D:\\program" + File.separator; // 上传文件的存放位置

        String filePath = rootPath + File.separator + "face" + File.separator + newFileName;
        File newFile = new File(filePath);
        // 判断目标文件所在目录是否存在
        if (!newFile.getParentFile().exists()) {
            // 如果目标文件所在目录不存在，则创建父级目录
            newFile.getParentFile().mkdirs();
        }

        // 将内存中的数据写入磁盘
        file.transferTo(newFile);

        return GraceJSONResult.ok();
    }


    // 分布式存储技术方案minIO实现文件上传（微服务项目推荐使用）
    @PostMapping("uploadFace")
    public GraceJSONResult uploadFace(@RequestParam("file") MultipartFile file,
                                      @NotNull(message = "用户ID不能为空") String userId) throws Exception {

        String filename = file.getOriginalFilename();   // 获得文件原始名称
        if (StringUtils.isBlank(filename)) {
            return GraceJSONResult.errorCustom(FILE_UPLOAD_FAILD);
        }
        // 定义存放路径，face/userId/filename
        filename = "face" + "/" + userId + "/" + filename;
        MinIOUtils.uploadFile(minIOConfig.getBucketName(),
                filename,
                file.getInputStream());

        String faceUrl = minIOConfig.getFileHost()
                + "/"
                + minIOConfig.getBucketName()
                + "/"
                + filename;

        return GraceJSONResult.ok(faceUrl);
    }
}
