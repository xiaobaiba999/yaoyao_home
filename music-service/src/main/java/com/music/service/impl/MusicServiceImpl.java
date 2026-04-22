package com.music.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.music.entity.Music;
import com.music.repository.MusicRepository;
import com.music.service.MusicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MusicServiceImpl implements MusicService {

    private final MusicRepository musicRepository;

    @Value("${music.storage-path}")
    private String storagePath;

    @Value("${music.base-url:}")
    private String baseUrl;

    @Value("${music.r2.endpoint:}")
    private String r2Endpoint;

    @Value("${music.r2.access-key-id:}")
    private String r2AccessKeyId;

    @Value("${music.r2.access-key-secret:}")
    private String r2AccessKeySecret;

    @Value("${music.r2.bucket-name:}")
    private String r2BucketName;

    @Value("${music.r2.public-url:}")
    private String r2PublicUrl;

    @Value("${music.r2.path-prefix:music}")
    private String r2PathPrefix;

    @Value("${music.oss.endpoint:}")
    private String ossEndpoint;

    @Value("${music.oss.access-key-id:}")
    private String ossAccessKeyId;

    @Value("${music.oss.access-key-secret:}")
    private String ossAccessKeySecret;

    @Value("${music.oss.bucket-name:}")
    private String ossBucketName;

    @Value("${music.oss.url-prefix:}")
    private String ossUrlPrefix;

    @Value("${music.oss.path-prefix:music}")
    private String ossPathPrefix;

    private S3Client s3Client;

    private boolean isR2Configured() {
        return r2Endpoint != null && !r2Endpoint.isEmpty()
                && r2AccessKeyId != null && !r2AccessKeyId.isEmpty()
                && r2AccessKeySecret != null && !r2AccessKeySecret.isEmpty()
                && r2BucketName != null && !r2BucketName.isEmpty();
    }

    private boolean isOssConfigured() {
        return ossEndpoint != null && !ossEndpoint.isEmpty()
                && ossAccessKeyId != null && !ossAccessKeyId.isEmpty()
                && ossAccessKeySecret != null && !ossAccessKeySecret.isEmpty()
                && ossBucketName != null && !ossBucketName.isEmpty();
    }

    @PostConstruct
    public void init() {
        File dir = new File(storagePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (isR2Configured()) {
            s3Client = S3Client.builder()
                    .endpointOverride(java.net.URI.create(r2Endpoint))
                    .region(Region.of("auto"))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(r2AccessKeyId, r2AccessKeySecret)
                    ))
                    .build();
            log.info("[MusicService] R2已配置，文件存储在Cloudflare R2");
        } else if (isOssConfigured()) {
            log.info("[MusicService] OSS已配置，文件存储在阿里云OSS");
        } else {
            syncDatabaseWithStorage();
        }
    }

    private void syncDatabaseWithStorage() {
        List<Music> allMusic = musicRepository.findAll();
        int missingCount = 0;

        for (Music music : allMusic) {
            File file = new File(storagePath, music.getFileName());
            if (!file.exists()) {
                missingCount++;
                log.warn("[MusicService] 本地文件不存在: id={}, name={}, fileName={}, fileUrl={}",
                        music.getId(), music.getName(), music.getFileName(), music.getFileUrl());
            }
        }

        if (missingCount > 0) {
            log.warn("[MusicService] 发现 {} 条音乐记录的本地文件缺失，但数据库记录保留（可能是FC重新部署导致临时存储清空）", missingCount);
            log.info("[MusicService] 提示：请配置R2/OSS存储以实现文件持久化，避免FC部署后文件丢失");
        } else {
            log.info("[MusicService] 所有音乐文件完整性检查通过，共 {} 首", allMusic.size());
        }
    }

    @Override
    public List<Music> getAllMusic() {
        return musicRepository.findAll();
    }

    @Override
    public Music getMusicById(Long id) {
        return musicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("音乐不存在"));
    }

    @Override
    public String getMusicPlayUrl(Long id) {
        Music music = getMusicById(id);
        return music.getFileUrl();
    }

    @Override
    @Transactional
    public Music uploadMusic(String name, String artist, String description, byte[] fileData, String originalFilename) {
        String extension = FileUtil.extName(originalFilename);
        String fileName = IdUtil.simpleUUID() + "." + extension;

        String fileUrl;

        if (isR2Configured()) {
            fileUrl = uploadToR2(fileName, fileData);
        } else if (isOssConfigured()) {
            fileUrl = uploadToOss(fileName, fileData);
        } else {
            uploadToLocal(fileName, fileData);
            if (baseUrl != null && !baseUrl.isEmpty()) {
                fileUrl = baseUrl + "/api/music/stream/" + fileName;
            } else {
                fileUrl = "/api/music/stream/" + fileName;
            }
        }

        Music music = new Music();
        music.setName(name);
        music.setArtist(artist);
        music.setDescription(description);
        music.setFileName(fileName);
        music.setFileUrl(fileUrl);
        music.setFileSize((long) fileData.length);

        return musicRepository.save(music);
    }

    private String uploadToR2(String fileName, byte[] fileData) {
        String objectKey = r2PathPrefix + "/" + fileName;
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(r2BucketName)
                    .key(objectKey)
                    .contentType("audio/mpeg")
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(new ByteArrayInputStream(fileData), fileData.length));
            log.info("[MusicService] R2上传成功: {}", objectKey);

            if (r2PublicUrl != null && !r2PublicUrl.isEmpty()) {
                String base = r2PublicUrl.endsWith("/") ? r2PublicUrl : r2PublicUrl + "/";
                return base + objectKey;
            }
            return "https://pub-" + r2BucketName + ".r2.dev/" + objectKey;
        } catch (Exception e) {
            log.error("[MusicService] R2上传失败", e);
            throw new RuntimeException("文件上传到R2失败", e);
        }
    }

    private String uploadToOss(String fileName, byte[] fileData) {
        String objectKey = ossPathPrefix + "/" + fileName;
        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(ossEndpoint, ossAccessKeyId, ossAccessKeySecret);
            ossClient.putObject(ossBucketName, objectKey, new ByteArrayInputStream(fileData));
            log.info("[MusicService] OSS上传成功: {}", objectKey);

            if (ossUrlPrefix != null && !ossUrlPrefix.isEmpty()) {
                String prefix = ossUrlPrefix.endsWith("/") ? ossUrlPrefix : ossUrlPrefix + "/";
                return prefix + objectKey;
            }
            return "https://" + ossBucketName + "." + ossEndpoint + "/" + objectKey;
        } catch (Exception e) {
            log.error("[MusicService] OSS上传失败", e);
            throw new RuntimeException("文件上传到OSS失败", e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    private void uploadToLocal(String fileName, byte[] fileData) {
        File destFile = new File(storagePath, fileName);
        try (FileOutputStream fos = new FileOutputStream(destFile)) {
            fos.write(fileData);
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败", e);
        }
    }

    @Override
    @Transactional
    public void deleteMusic(Long id) {
        Music music = musicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("音乐不存在"));

        try {
            if (isR2Configured() && music.getFileUrl() != null &&
                    (music.getFileUrl().contains("r2.dev") || music.getFileUrl().contains("cloudflare"))) {
                deleteFromR2(music.getFileName());
            } else if (isOssConfigured() && music.getFileUrl() != null && music.getFileUrl().contains("aliyuncs.com")) {
                deleteFromOss(music.getFileName());
            } else {
                File file = new File(storagePath, music.getFileName());
                if (file.exists()) {
                    file.delete();
                }
            }
        } catch (Exception e) {
            log.warn("[MusicService] 删除存储文件失败: {}", e.getMessage());
        }

        musicRepository.deleteById(id);
    }

    private void deleteFromR2(String fileName) {
        String objectKey = r2PathPrefix + "/" + fileName;
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(r2BucketName)
                    .key(objectKey)
                    .build();
            s3Client.deleteObject(deleteRequest);
            log.info("[MusicService] R2删除成功: {}", objectKey);
        } catch (Exception e) {
            log.error("[MusicService] R2删除失败: {}", objectKey, e);
        }
    }

    private void deleteFromOss(String fileName) {
        String objectKey = ossPathPrefix + "/" + fileName;
        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(ossEndpoint, ossAccessKeyId, ossAccessKeySecret);
            ossClient.deleteObject(ossBucketName, objectKey);
            log.info("[MusicService] OSS删除成功: {}", objectKey);
        } catch (Exception e) {
            log.error("[MusicService] OSS删除失败: {}", objectKey, e);
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }
}
