package com.music.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.music.entity.Music;
import com.music.repository.MusicRepository;
import com.music.service.MusicService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MusicServiceImpl implements MusicService {

    private final MusicRepository musicRepository;

    @Value("${music.storage-path}")
    private String storagePath;

    @Value("${music.base-url:}")
    private String baseUrl;

    @PostConstruct
    public void init() {
        File dir = new File(storagePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Override
    public List<Music> getAllMusic() {
        return musicRepository.findAll();
    }

    @Override
    public Music getMusicById(Long id) {
        return musicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("音乐不存在"));
    }

    @Override
    public String getMusicPlayUrl(Long id) {
        Music music = getMusicById(id);
        return music.getFileUrl();
    }

    @Override
    @Transactional
    public Music uploadMusic(String name, String artist, String description, byte[] fileData, String originalFilename) {
        // 生成唯一文件名
        String extension = FileUtil.extName(originalFilename);
        String fileName = IdUtil.simpleUUID() + "." + extension;
        
        // 保存文件
        File destFile = new File(storagePath, fileName);
        try (FileOutputStream fos = new FileOutputStream(destFile)) {
            fos.write(fileData);
        } catch (IOException e) {
            throw new RuntimeException("文件保存失败", e);
        }

        // 构建访问URL
        String fileUrl;
        if (baseUrl != null && !baseUrl.isEmpty()) {
            fileUrl = baseUrl + "/api/music/stream/" + fileName;
        } else {
            fileUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .scheme("https")
                    .path("/api/music/stream/")
                    .path(fileName)
                    .toUriString();
        }

        // 保存到数据库
        Music music = new Music();
        music.setName(name);
        music.setArtist(artist);
        music.setDescription(description);
        music.setFileName(fileName);
        music.setFileUrl(fileUrl);
        music.setFileSize((long) fileData.length);

        return musicRepository.save(music);
    }

    @Override
    @Transactional
    public void deleteMusic(Long id) {
        Music music = musicRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("音乐不存在"));
        
        // 删除文件
        File file = new File(storagePath, music.getFileName());
        if (file.exists()) {
            file.delete();
        }
        
        // 删除记录
        musicRepository.deleteById(id);
    }
}
