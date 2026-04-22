package com.yaoyao.service;

import org.springframework.web.multipart.MultipartFile;

public interface OssStorageService {
    String uploadFile(MultipartFile file);
    void deleteFile(String fileUrl);
    String getFileUrl(String objectKey);
}
