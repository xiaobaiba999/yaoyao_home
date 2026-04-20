package com.yaoyao.service;

import org.springframework.web.multipart.MultipartFile;

public interface GiteeStorageService {

    String uploadFile(MultipartFile file);

    void deleteFile(String filename);

    String getFileUrl(String filename);
}
