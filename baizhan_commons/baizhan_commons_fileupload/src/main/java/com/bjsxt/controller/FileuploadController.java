package com.bjsxt.controller;

import com.bjsxt.commons.pojo.BaizhanResult;
import com.bjsxt.service.FileuploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
public class FileuploadController {
    @Autowired
    private FileuploadService fileuploadService;
    /**
     * 上传文件到FastDFS
     * @return
     *  图片可访问地址。
     *   http://stroage服务器IP:nginx端口/组名/M00/文件名
     */
    @PostMapping("/file/upload")
    public BaizhanResult uploadFile(MultipartFile file){
        try {
            log.info("上传图片到FastDFS");
            return fileuploadService.uploadFile(file);
        }catch (Exception e){
            log.error(e.getMessage());
            return BaizhanResult.error("服务器忙，请稍后重试");
        }
    }
}
