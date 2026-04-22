package com.yaoyao.controller;

import com.yaoyao.common.Result;
import com.yaoyao.entity.Photo;
import com.yaoyao.service.PhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
public class PhotoController {

    private final PhotoService photoService;

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
            byte[] imageData;
            String contentType = "image/png";

            if (url.startsWith("http://") || url.startsWith("https://")) {
                HttpClient client = HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(10))
                        .build();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .timeout(Duration.ofSeconds(30))
                        .GET()
                        .build();
                HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
                imageData = response.body();
            } else {
                return ResponseEntity.notFound().build();
            }

            String filename = photo.getOriginalName();
            if (filename == null || filename.isEmpty()) {
                filename = photo.getFilename();
            }
            if (filename == null || filename.isEmpty()) {
                filename = "photo.png";
            }

            if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) {
                contentType = "image/jpeg";
            } else if (filename.toLowerCase().endsWith(".gif")) {
                contentType = "image/gif";
            } else if (filename.toLowerCase().endsWith(".webp")) {
                contentType = "image/webp";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + java.net.URLEncoder.encode(filename, "UTF-8") + "\"")
                    .header(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                    .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                    .body(imageData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
