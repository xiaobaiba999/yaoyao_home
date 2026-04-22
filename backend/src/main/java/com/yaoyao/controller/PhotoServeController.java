package com.yaoyao.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.yaoyao.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
@RestController
@RequestMapping("/api/photos/serve")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PhotoServeController {

    private final AppProperties appProperties;

    @GetMapping("/{objectKey:.*}")
    public void servePhoto(@PathVariable String objectKey, HttpServletResponse response) {
        try {
            log.info("[PhotoServe] 请求图片: {}", objectKey);

            if (appProperties.isOssConfigured()) {
                serveFromOss(objectKey, response);
            } else {
                serveFromLocal(objectKey, response);
            }
        } catch (Exception e) {
            log.error("[PhotoServe] 服务图片失败: {}", objectKey, e);
            try {
                response.setStatus(500);
                response.getWriter().write("服务图片失败");
            } catch (Exception ignored) {}
        }
    }

    private void serveFromOss(String objectKey, HttpServletResponse response) throws Exception {
        AppProperties.Oss ossConfig = appProperties.getOss();
        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(
                    ossConfig.getEndpoint(),
                    ossConfig.getAccessKeyId(),
                    ossConfig.getAccessKeySecret()
            );

            InputStream inputStream = ossClient.getObject(ossConfig.getBucketName(), objectKey).getObjectContent();
            if (inputStream == null) {
                response.setStatus(404);
                response.getWriter().write("图片不存在");
                return;
            }

            setResponseHeaders(response, objectKey);
            inputStream.transferTo(response.getOutputStream());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    private void serveFromLocal(String objectKey, HttpServletResponse response) throws Exception {
        String uploadDir = appProperties.getUploadDir();
        Path filePath = Paths.get(uploadDir, objectKey).normalize();
        File file = filePath.toFile();

        if (!file.exists()) {
            log.warn("[PhotoServe] 本地文件不存在: {}", filePath);
            response.setStatus(404);
            response.getWriter().write("图片不存在（本地存储）");
            return;
        }

        setResponseHeaders(response, objectKey);
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.transferTo(response.getOutputStream());
        }
    }

    private void setResponseHeaders(HttpServletResponse response, String objectKey) {
        response.setContentType(getContentType(objectKey));
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Cache-Control", "public, max-age=86400");
    }

    private String getContentType(String objectKey) {
        if (objectKey.endsWith(".png")) return MediaType.IMAGE_PNG_VALUE;
        if (objectKey.endsWith(".jpg") || objectKey.endsWith(".jpeg")) return MediaType.IMAGE_JPEG_VALUE;
        if (objectKey.endsWith(".gif")) return MediaType.IMAGE_GIF_VALUE;
        if (objectKey.endsWith(".webp")) return "image/webp";
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}
