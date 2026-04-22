package com.yaoyao.controller;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.yaoyao.common.Result;
import com.yaoyao.config.AppProperties;
import com.yaoyao.entity.Photo;
import com.yaoyao.service.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class PhotoController {

    private final PhotoService photoService;
    private final AppProperties appProperties;

    @GetMapping
    public Result<List<Photo>> list() {
        List<Photo> data = photoService.list();
        return Result.success(data);
    }

    @PostMapping
    public Result<Photo> upload(
            @RequestParam("photo") MultipartFile file,
            @RequestParam(value = "description", required = false, defaultValue = "") String description) {
        Photo data = photoService.upload(file, description);
        return Result.success(data);
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable String id) {
        photoService.delete(id);
        return Result.success("删除成功");
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> download(@PathVariable String id) {
        try {
            Photo photo = photoService.getById(id);
            if (photo == null) {
                return ResponseEntity.notFound().build();
            }

            String url = photo.getUrl();
            byte[] imageData = null;

            if (url.startsWith("http://") || url.startsWith("https://")) {
                imageData = downloadFromUrl(url);
            } else if (appProperties.isOssConfigured()) {
                imageData = downloadFromOss(url);
            } else {
                imageData = downloadFromLocal(url);
            }

            if (imageData == null || imageData.length == 0) {
                return ResponseEntity.notFound().build();
            }

            String filename = photo.getOriginalName();
            if (filename == null || filename.isEmpty()) {
                filename = photo.getFilename();
            }
            if (filename == null || filename.isEmpty()) {
                filename = "photo.png";
            }

            String contentType = getContentType(filename);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + java.net.URLEncoder.encode(filename, "UTF-8") + "\"")
                    .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                    .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition")
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                    .body(imageData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private byte[] downloadFromUrl(String url) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();
        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        return response.body();
    }

    private byte[] downloadFromOss(String objectKey) {
        AppProperties.Oss ossConfig = appProperties.getOss();
        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(
                    ossConfig.getEndpoint(),
                    ossConfig.getAccessKeyId(),
                    ossConfig.getAccessKeySecret()
            );
            InputStream inputStream = ossClient.getObject(ossConfig.getBucketName(), objectKey).getObjectContent();
            if (inputStream == null) return null;
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            inputStream.transferTo(buffer);
            return buffer.toByteArray();
        } catch (Exception e) {
            return null;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    private byte[] downloadFromLocal(String path) {
        try {
            String uploadDir = appProperties.getUploadDir();
            File file = new File(uploadDir, path);
            if (!file.exists()) return null;
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            fis.transferTo(buffer);
            fis.close();
            return buffer.toByteArray();
        } catch (Exception e) {
            return null;
        }
    }

    private String getContentType(String filename) {
        if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (filename.toLowerCase().endsWith(".gif")) {
            return "image/gif";
        } else if (filename.toLowerCase().endsWith(".webp")) {
            return "image/webp";
        }
        return "image/png";
    }
}
