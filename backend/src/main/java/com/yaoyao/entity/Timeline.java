package com.yaoyao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("timeline")
public class Timeline implements Serializable {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String date;
    private String title;
    private String description;
    private LocalDateTime createdAt;
}
