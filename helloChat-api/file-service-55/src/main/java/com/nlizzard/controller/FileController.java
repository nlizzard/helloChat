package com.nlizzard.controller;

import com.nlizzard.grace.result.GraceJSONResult;
import io.micrometer.common.util.StringUtils;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
@RequestMapping("file")
@Validated
public class FileController {

    // springboot文件上传实现方案
    @PostMapping("uploadFace")
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
}
