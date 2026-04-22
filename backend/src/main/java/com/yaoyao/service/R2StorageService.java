package com.yaoyao.service;

import org.springframework.web.multipart.MultipartFile;

public interface R2StorageService {
    String uploadFile(MultipartFile file);
    void deleteFile(String objectKey);
    String getFileUrl(String objectKey);
}
