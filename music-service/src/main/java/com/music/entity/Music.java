package com.music.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "music")
public class Music {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String artist;
    
    @Column(nullable = false, unique = true)
    private String fileName;
    
    @Column(nullable = false)
    private String fileUrl;
    
    private Long fileSize;
    
    private String duration;
    
    private String description;
    
    @Column(nullable = false)
    private LocalDateTime createTime;
    
    @PrePersist
    public void prePersist() {
        this.createTime = LocalDateTime.now();
    }
}
