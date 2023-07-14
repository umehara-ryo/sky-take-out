package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;


    @ApiOperation(value = "文件上传")
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传：{}",file);
        try {
            //1.获取文件名
            String originalFilename = file.getOriginalFilename();

            //2.拡張子を獲得
            String k = originalFilename.substring(originalFilename.lastIndexOf("."));

            //3.新しいファイル名を作る
            String fileName = UUID.randomUUID().toString() + k;

            //4.アップロードし、urlアドレスをもらう
            String url = aliOssUtil.upload(file.getBytes(), fileName);

            return Result.success(url);
        } catch (IOException e) {
            log.info("ファイルのアップロード 失敗：{}",e  );
            return Result.error(MessageConstant.UPLOAD_FAILED);
        }



    }
}
