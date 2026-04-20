package com.yaoyao.service;

import org.springframework.web.multipart.MultipartFile;

public interface LocalStorageService {
    String uploadFile(MultipartFile file);
    void deleteFile(String filename);
    String getFileUrl(String filename);
}
