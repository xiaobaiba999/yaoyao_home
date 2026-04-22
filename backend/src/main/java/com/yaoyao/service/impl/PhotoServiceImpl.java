package com.yaoyao.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yaoyao.config.AppProperties;
import com.yaoyao.entity.Photo;
import com.yaoyao.mapper.PhotoMapper;
import com.yaoyao.service.GiteeStorageService;
import com.yaoyao.service.LocalStorageService;
import com.yaoyao.service.PhotoService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PhotoServiceImpl implements PhotoService {

    private static final Logger log = LoggerFactory.getLogger(PhotoServiceImpl.class);
    private final PhotoMapper photoMapper;
    private final GiteeStorageService giteeStorageService;
    private final LocalStorageService localStorageService;
    private final AppProperties appProperties;

    @PostConstruct
    public void init() {
        syncDatabaseWithStorage();
    }

    private void syncDatabaseWithStorage() {
        List<Photo> allPhotos = photoMapper.selectList(null);
        List<String> missingIds = new ArrayList<>();

        for (Photo photo : allPhotos) {
            String url = photo.getUrl();
            if (url != null && url.startsWith("/uploads/")) {
                String filename = url.substring("/uploads/".length());
                String uploadDir = appProperties.getUploadDir();
                File file = new File(uploadDir, filename);
                if (!file.exists()) {
                    missingIds.add(photo.getId());
                    log.warn("[PhotoService] 图片文件不存在，标记清理: id={}, url={}", photo.getId(), url);
                }
            }
        }

        if (!missingIds.isEmpty()) {
            log.info("[PhotoService] 清理 {} 条无效图片记录（文件已丢失）", missingIds.size());
            for (String id : missingIds) {
                photoMapper.deleteById(id);
            }
        } else {
            log.info("[PhotoService] 所有图片文件完整性检查通过，共 {} 张", allPhotos.size());
        }
    }

    @Override
    public List<Photo> list() {
        LambdaQueryWrapper<Photo> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Photo::getCreatedAt);
        return photoMapper.selectList(wrapper);
    }

    @Override
    public Photo upload(MultipartFile file, String description) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择要上传的图片");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("只支持图片文件 (jpeg, jpg, png, gif, webp)");
        }

        String url;
        if (appProperties.isGiteeConfigured()) {
            url = giteeStorageService.uploadFile(file);
        } else {
            log.info("Gitee未配置，使用本地存储");
            url = localStorageService.uploadFile(file);
        }

        String desc = "";
        if (StrUtil.isNotBlank(description)) {
            desc = description.trim().substring(0, Math.min(description.trim().length(), 20));
        }

        Photo photo = new Photo();
        photo.setFilename(file.getOriginalFilename());
        photo.setOriginalName(file.getOriginalFilename());
        photo.setUrl(url);
        photo.setDescription(desc);
        photo.setCreatedAt(LocalDateTime.now());
        photoMapper.insert(photo);
        return photo;
    }

    @Override
    public void delete(String id) {
        Photo photo = photoMapper.selectById(id);
        if (photo != null) {
            if (StrUtil.isNotBlank(photo.getUrl()) && photo.getUrl().contains("gitee.com")) {
                try {
                    giteeStorageService.deleteFile(photo.getFilename());
                } catch (Exception e) {
                    log.warn("删除Gitee文件失败: {}", e.getMessage());
                }
            } else if (StrUtil.isNotBlank(photo.getUrl()) && photo.getUrl().startsWith("/uploads/")) {
                String filename = photo.getUrl().substring("/uploads/".length());
                localStorageService.deleteFile(filename);
            }
        }
        photoMapper.deleteById(id);
    }
}
