package com.yaoyao.service.impl;

import com.yaoyao.config.AppProperties;
import com.yaoyao.service.R2StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
public class R2StorageServiceImpl implements R2StorageService {

    private final AppProperties.R2 r2Config;
    private final S3Client s3Client;

    public R2StorageServiceImpl(AppProperties appProperties) {
        this.r2Config = appProperties.getR2();
        this.s3Client = S3Client.builder()
                .endpointOverride(java.net.URI.create(r2Config.getEndpoint()))
                .region(Region.of(r2Config.getRegion() != null ? r2Config.getRegion() : "auto"))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(r2Config.getAccessKeyId(), r2Config.getAccessKeySecret())
                ))
                .build();
        log.info("[R2] 初始化完成, endpoint={}, bucket={}", r2Config.getEndpoint(), r2Config.getBucketName());
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
        String objectKey = r2Config.getPathPrefix() + "/" + filename;

        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(r2Config.getBucketName())
                    .key(objectKey)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));
            log.info("[R2] 上传成功: {}", objectKey);
            return getFileUrl(objectKey);
        } catch (IOException e) {
            log.error("[R2] 上传失败", e);
            throw new RuntimeException("文件上传到R2失败", e);
        }
    }

    @Override
    public void deleteFile(String objectKey) {
        if (objectKey == null || objectKey.isEmpty()) return;
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(r2Config.getBucketName())
                    .key(objectKey)
                    .build();
            s3Client.deleteObject(deleteRequest);
            log.info("[R2] 删除成功: {}", objectKey);
        } catch (Exception e) {
            log.error("[R2] 删除失败: {}", objectKey, e);
        }
    }

    @Override
    public String getFileUrl(String objectKey) {
        if (r2Config.getPublicUrl() != null && !r2Config.getPublicUrl().isEmpty()) {
            String base = r2Config.getPublicUrl();
            return base.endsWith("/") ? base + objectKey : base + "/" + objectKey;
        }
        return "https://" + r2Config.getBucketName() + ".r2.cloudflarestorage.com/" + objectKey;
    }

    public String extractObjectKey(String url) {
        if (url == null || url.isEmpty()) return null;
        String prefix = r2Config.getPathPrefix() + "/";
        int idx = url.indexOf(prefix);
        if (idx >= 0) {
            return url.substring(idx);
        }
        return null;
    }
}
