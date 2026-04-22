package com.yaoyao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("operation_logs")
public class OperationLog {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String module;

    private String operation;

    private String description;

    private String method;

    private String requestParams;

    private Integer responseStatus;

    private String requestIp;

    private String userAgent;

    private Long duration;

    private String errorMessage;

    private LocalDateTime createdAt;
}
