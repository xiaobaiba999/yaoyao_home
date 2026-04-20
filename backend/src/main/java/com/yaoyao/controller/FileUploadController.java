package com.yaoyao.controller;

import com.yaoyao.common.Result;
import com.yaoyao.service.GiteeStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final GiteeStorageService giteeStorageService;

    @PostMapping
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error(400, "请选择要上传的文件");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error(400, "只支持图片文件 (jpeg, jpg, png, gif, webp)");
        }

        String url = giteeStorageService.uploadFile(file);
        return Result.success(Map.of("url", url, "filename", file.getOriginalFilename()));
    }
}
