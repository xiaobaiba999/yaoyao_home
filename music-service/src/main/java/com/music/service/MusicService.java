package com.music.service;

import com.music.entity.Music;
import java.util.List;

public interface MusicService {
    List<Music> getAllMusic();
    Music getMusicById(Long id);
    String getMusicPlayUrl(Long id);
    Music uploadMusic(String name, String artist, String description, byte[] fileData, String originalFilename);
    void deleteMusic(Long id);
}
