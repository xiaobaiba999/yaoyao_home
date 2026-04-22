package com.yaoyao.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yaoyao.config.AppProperties;
import com.yaoyao.entity.Photo;
import com.yaoyao.mapper.PhotoMapper;
import com.yaoyao.service.GiteeStorageService;
import com.yaoyao.service.LocalStorageService;
import com.yaoyao.service.OssStorageService;
import com.yaoyao.service.PhotoService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PhotoServiceImpl implements PhotoService {

    private static final Logger log = LoggerFactory.getLogger(PhotoServiceImpl.class);
    private final PhotoMapper photoMapper;
    private final OssStorageService ossStorageService;
    private final GiteeStorageService giteeStorageService;
    private final LocalStorageService localStorageService;
    private final AppProperties appProperties;

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
        if (appProperties.isOssConfigured()) {
            url = ossStorageService.uploadFile(file);
            log.info("[PhotoService] 使用OSS存储: {}", url);
        } else if (appProperties.isGiteeConfigured()) {
            url = giteeStorageService.uploadFile(file);
            log.info("[PhotoService] 使用Gitee存储: {}", url);
        } else {
            url = localStorageService.uploadFile(file);
            log.info("[PhotoService] 使用本地存储: {}", url);
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
        if (photo != null && StrUtil.isNotBlank(photo.getUrl())) {
            String url = photo.getUrl();
            try {
                if (url.contains("aliyuncs.com") && appProperties.isOssConfigured()) {
                    ossStorageService.deleteFile(url);
                } else if (url.contains("gitee.com")) {
                    giteeStorageService.deleteFile(photo.getFilename());
                } else if (url.startsWith("/uploads/")) {
                    String filename = url.substring("/uploads/".length());
                    localStorageService.deleteFile(filename);
                }
            } catch (Exception e) {
                log.warn("[PhotoService] 删除存储文件失败: {}", e.getMessage());
            }
        }
        photoMapper.deleteById(id);
    }
}
