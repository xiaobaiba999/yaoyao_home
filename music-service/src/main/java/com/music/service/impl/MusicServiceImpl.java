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

import jakarta.annotation.PostConstruct;
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
        if (!isOssConfigured()) {
            syncDatabaseWithStorage();
        } else {
            log.info("[MusicService] OSS已配置，文件存储在云端，无需本地同步");
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
            log.info("[MusicService] 提示：请配置OSS存储以实现文件持久化，避免FC部署后文件丢失");
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

        if (isOssConfigured()) {
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

    private String uploadToOss(String fileName, byte[] fileData) {
        String objectKey = ossPathPrefix + "/" + fileName;
        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(ossEndpoint, ossAccessKeyId, ossAccessKeySecret);
            ossClient.putObject(ossBucketName, objectKey, new java.io.ByteArrayInputStream(fileData));
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

        if (isOssConfigured() && music.getFileUrl() != null && music.getFileUrl().contains("aliyuncs.com")) {
            deleteFromOss(music.getFileName());
        } else {
            File file = new File(storagePath, music.getFileName());
            if (file.exists()) {
                file.delete();
            }
        }

        musicRepository.deleteById(id);
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
