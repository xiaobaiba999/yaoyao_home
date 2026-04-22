package com.yaoyao.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.yaoyao.config.AppProperties;
import com.yaoyao.service.OssStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
public class OssStorageServiceImpl implements OssStorageService {

    private final AppProperties.Oss ossConfig;

    public OssStorageServiceImpl(AppProperties appProperties) {
        this.ossConfig = appProperties.getOss();
    }

    @Override
    public String uploadFile(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = "photo-" + System.currentTimeMillis()
                + "-" + UUID.randomUUID().toString().substring(0, 8) + extension;
        String objectKey = ossConfig.getPathPrefix() + "/" + filename;

        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(
                    ossConfig.getEndpoint(),
                    ossConfig.getAccessKeyId(),
                    ossConfig.getAccessKeySecret()
            );
            try (InputStream inputStream = file.getInputStream()) {
                ossClient.putObject(ossConfig.getBucketName(), objectKey, inputStream);
            }
            log.info("[OSS] 上传成功: {}", objectKey);
            return getFileUrl(objectKey);
        } catch (IOException e) {
            log.error("[OSS] 上传失败", e);
            throw new RuntimeException("文件上传到OSS失败", e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    @Override
    public void deleteFile(String fileUrl) {
        String objectKey = extractObjectKey(fileUrl);
        if (objectKey == null) {
            log.warn("[OSS] 无法从URL提取objectKey: {}", fileUrl);
            return;
        }

        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(
                    ossConfig.getEndpoint(),
                    ossConfig.getAccessKeyId(),
                    ossConfig.getAccessKeySecret()
            );
            ossClient.deleteObject(ossConfig.getBucketName(), objectKey);
            log.info("[OSS] 删除成功: {}", objectKey);
        } catch (Exception e) {
            log.error("[OSS] 删除失败: {}", objectKey, e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    @Override
    public String getFileUrl(String objectKey) {
        return "https://" + ossConfig.getBucketName() + "." + ossConfig.getEndpoint() + "/" + objectKey;
    }

    private String extractObjectKey(String url) {
        if (url == null || url.isEmpty()) return null;
        String prefix = ossConfig.getPathPrefix() + "/";
        int idx = url.indexOf(prefix);
        if (idx >= 0) {
            return url.substring(idx);
        }
        return null;
    }
}
