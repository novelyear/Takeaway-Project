package com.my.controller.admin;

import com.my.constant.MessageConstant;
import com.my.result.Result;
import com.my.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;
    @Autowired
    private HttpMessageConverters messageConverters;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @ApiOperation("上传文件")
    public Result<String> upload(MultipartFile file) {
        log.info("文件上传：{}", file.getOriginalFilename());
        try {
            String originalFilename = file.getOriginalFilename();
            String extention = originalFilename.substring(originalFilename.lastIndexOf("."));
            String objName = UUID.randomUUID().toString() + extention;
            String path = aliOssUtil.upload(file.getBytes(), objName);
            return Result.success(path);
        } catch (IOException e) {
            return Result.error(MessageConstant.UPLOAD_FAILED);
        }
    }

}
