package com.music.repository;

import com.music.entity.Music;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MusicRepository extends JpaRepository<Music, Long> {
    Optional<Music> findByFileName(String fileName);
}
