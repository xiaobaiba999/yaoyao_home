package com.yaoyao.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.yaoyao.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.io.InputStream;

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

            if (!appProperties.isOssConfigured()) {
                response.setStatus(503);
                response.getWriter().write("OSS未配置");
                return;
            }

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

                String contentType = getContentType(objectKey);
                response.setContentType(contentType);
                response.setHeader("Access-Control-Allow-Origin", "*");
                response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
                response.setHeader("Access-Control-Allow-Headers", "*");
                response.setHeader("Cache-Control", "public, max-age=86400");

                inputStream.transferTo(response.getOutputStream());
                response.setStatus(200);
            } finally {
                if (ossClient != null) {
                    ossClient.shutdown();
                }
            }
        } catch (Exception e) {
            log.error("[PhotoServe] 服务图片失败", e);
            response.setStatus(500);
            try {
                response.getWriter().write("服务图片失败: " + e.getMessage());
            } catch (Exception ignored) {}
        }
    }

    private String getContentType(String objectKey) {
        if (objectKey.endsWith(".png")) return MediaType.IMAGE_PNG_VALUE;
        if (objectKey.endsWith(".jpg") || objectKey.endsWith(".jpeg")) return MediaType.IMAGE_JPEG_VALUE;
        if (objectKey.endsWith(".gif")) return MediaType.IMAGE_GIF_VALUE;
        if (objectKey.endsWith(".webp")) return "image/webp";
        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}
