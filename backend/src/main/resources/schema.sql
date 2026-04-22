-- MySQL 8.0 数据库初始化脚本
-- 保持与原 Supabase 表结构完全一致

CREATE DATABASE IF NOT EXISTS yaoyao_memorial DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE yaoyao_memorial;

CREATE TABLE IF NOT EXISTS photos (
  id CHAR(36) PRIMARY KEY,
  filename VARCHAR(255) NOT NULL,
  original_name VARCHAR(255),
  url TEXT NOT NULL,
  description VARCHAR(255) DEFAULT '',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS messages (
  id CHAR(36) PRIMARY KEY,
  content TEXT NOT NULL,
  author VARCHAR(100) DEFAULT '匿名',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS diaries (
  id CHAR(36) PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  content TEXT NOT NULL,
  mood VARCHAR(50) DEFAULT 'happy',
  date DATE NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL
);

CREATE TABLE IF NOT EXISTS wishes (
  id CHAR(36) PRIMARY KEY,
  content TEXT NOT NULL,
  completed TINYINT(1) DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL
);

CREATE TABLE IF NOT EXISTS plans (
  id CHAR(36) PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  target_date DATE,
  status VARCHAR(20) DEFAULT 'pending',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NULL,
  CONSTRAINT chk_status CHECK (status IN ('pending', 'in_progress', 'completed'))
);

CREATE TABLE IF NOT EXISTS timeline (
  id CHAR(36) PRIMARY KEY,
  date VARCHAR(50) NOT NULL,
  title VARCHAR(255) NOT NULL,
  description TEXT NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_photos_created_at ON photos(created_at DESC);
CREATE INDEX idx_messages_created_at ON messages(created_at DESC);
CREATE INDEX idx_diaries_date ON diaries(date DESC);
CREATE INDEX idx_wishes_created_at ON wishes(created_at DESC);
CREATE INDEX idx_plans_target_date ON plans(target_date);
CREATE INDEX idx_timeline_created_at ON timeline(created_at DESC);

CREATE TABLE IF NOT EXISTS operation_logs (
  id CHAR(36) PRIMARY KEY,
  module VARCHAR(50) NOT NULL COMMENT '操作模块',
  operation VARCHAR(50) NOT NULL COMMENT '操作类型',
  description VARCHAR(500) COMMENT '操作描述',
  method VARCHAR(200) COMMENT '请求方法',
  request_params TEXT COMMENT '请求参数',
  response_status INT COMMENT '响应状态码',
  request_ip VARCHAR(50) COMMENT '请求IP',
  user_agent VARCHAR(500) COMMENT '用户代理',
  duration BIGINT COMMENT '执行时长(ms)',
  error_message TEXT COMMENT '错误信息',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_operation_logs_created_at ON operation_logs(created_at DESC);
CREATE INDEX idx_operation_logs_module ON operation_logs(module);
CREATE INDEX idx_operation_logs_operation ON operation_logs(operation);
