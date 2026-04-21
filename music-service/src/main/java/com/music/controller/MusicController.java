package com.music.controller;

import com.music.entity.Music;
import com.music.service.MusicService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/music")
@RequiredArgsConstructor
public class MusicController {

    private final MusicService musicService;

    @Value("${music.storage-path}")
    private String storagePath;

    /**
     * 获取所有音乐列表
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> list() {
        List<Music> musicList = musicService.getAllMusic();
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", musicList);
        return ResponseEntity.ok(result);
    }

    /**
     * 上传音乐文件
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "artist", required = false, defaultValue = "未知") String artist,
            @RequestParam(value = "description", required = false, defaultValue = "") String description) {
        
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "message", "文件不能为空"
                ));
            }

            byte[] fileData = file.getBytes();
            String originalFilename = file.getOriginalFilename();
            String displayName = name != null ? name : originalFilename.replaceAll("\\.[^.]+$", "");

            Music music = musicService.uploadMusic(displayName, artist, description, fileData, originalFilename);

            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "上传成功",
                "data", Map.of(
                    "id", music.getId(),
                    "name", music.getName(),
                    "artist", music.getArtist(),
                    "url", music.getFileUrl()
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "code", 500,
                "message", "上传失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 删除音乐
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        try {
            musicService.deleteMusic(id);
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "删除成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "code", 500,
                "message", "删除失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 播放/流式传输音乐文件
     */
    @GetMapping("/stream/{fileName}")
    public ResponseEntity<Resource> stream(@PathVariable String fileName) {
        File musicFile = new File(storagePath, fileName);
        
        if (!musicFile.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(musicFile);
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("audio/mpeg"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=31536000")
                .body(resource);
    }

    /**
     * 批量上传音乐
     */
    @PostMapping("/upload/batch")
    public ResponseEntity<Map<String, Object>> batchUpload(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "artist", required = false, defaultValue = "未知") String artist) {
        
        try {
            List<Map<String, Object>> results = new java.util.ArrayList<>();
            
            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;
                
                byte[] fileData = file.getBytes();
                String originalFilename = file.getOriginalFilename();
                String name = originalFilename.replaceAll("\\.[^.]+$", "");

                Music music = musicService.uploadMusic(name, artist, "", fileData, originalFilename);
                
                results.add(Map.of(
                    "id", music.getId(),
                    "name", music.getName(),
                    "url", music.getFileUrl()
                ));
            }

            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "批量上传成功",
                "data", results
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "code", 500,
                "message", "上传失败: " + e.getMessage()
            ));
        }
    }
}
