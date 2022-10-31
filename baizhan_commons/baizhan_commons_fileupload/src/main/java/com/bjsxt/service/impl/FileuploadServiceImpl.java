package com.bjsxt.service.impl;

import com.bjsxt.commons.pojo.BaizhanResult;
import com.bjsxt.service.FileuploadService;
import com.bjsxt.utils.FastDFSClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传微服务，服务实现类型
 */
@Service
public class FileuploadServiceImpl implements FileuploadService {
    // 把application-commonspojo中的配置注入到当前属性
    @Value("${baizhan.fastdfs.nginx}")
    private String nginxServer;

    @Override
    public BaizhanResult uploadFile(MultipartFile file) {
        // 调用工具的上传文件方法
        try {
            String[] result =
                    FastDFSClient.uploadFile(file.getInputStream(),
                            file.getOriginalFilename());
            if(result == null){
                throw new RuntimeException("上传文件工具类发生错误");
            }
            String path = nginxServer + result[0] + "/" + result[1];
            return BaizhanResult.ok(path);
        }catch (Exception e){
            throw new RuntimeException("上传图片时发生错误：" + e.getMessage(), e);
        }
    }
}
