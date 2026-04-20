package com.yaoyao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("photos")
public class Photo implements Serializable {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String filename;
    private String originalName;
    private String url;
    private String description;
    private LocalDateTime createdAt;
}
