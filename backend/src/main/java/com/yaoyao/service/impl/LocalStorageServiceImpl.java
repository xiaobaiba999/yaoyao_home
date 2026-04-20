package com.yaoyao.service.impl;

import cn.hutool.core.util.StrUtil;
import com.yaoyao.config.AppProperties;
import com.yaoyao.service.GiteeStorageService;
import com.yaoyao.service.LocalStorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocalStorageServiceImpl implements LocalStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalStorageServiceImpl.class);
    private final AppProperties appProperties;

    @Override
    public String uploadFile(MultipartFile file) {
        try {
            String uploadDir = appProperties.getUploadDir();
            if (StrUtil.isBlank(uploadDir)) {
                uploadDir = "./uploads";
            }

            Path dirPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (StrUtil.isNotBlank(originalFilename) && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = "photo-" + System.currentTimeMillis() 
                    + "-" + UUID.randomUUID().toString().substring(0, 8) + ext;

            Path targetPath = dirPath.resolve(filename).normalize();
            file.transferTo(targetPath.toFile());

            return "/uploads/" + filename;
        } catch (IOException e) {
            log.error("本地文件上传失败", e);
            throw new RuntimeException("文件上传失败");
        }
    }

    @Override
    public void deleteFile(String filename) {
        try {
            String uploadDir = appProperties.getUploadDir();
            if (StrUtil.isBlank(uploadDir)) {
                uploadDir = "./uploads";
            }
            Path filePath = Paths.get(uploadDir, filename).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("删除本地文件失败: {}", e.getMessage());
        }
    }

    @Override
    public String getFileUrl(String filename) {
        return "/uploads/" + filename;
    }
}
