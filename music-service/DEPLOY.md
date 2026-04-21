# 音乐服务线上部署指南

## 📦 项目结构

```
girlfrend/
├── frontend/           # 情侣网站前端（Vercel）
├── backend/            # 情侣网站后端（阿里云函数计算FC）
├── music-service/      # 音乐服务（需要部署到阿里云函数计算FC）
└── music-files/        # 音乐文件存储目录
```

## 🚀 部署步骤

### 1. 准备MySQL数据库

#### 1.1 创建数据库

在你的MySQL服务器上执行：

```sql
CREATE DATABASE IF NOT EXISTS music_service DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 1.2 获取数据库连接信息

- **主机地址**: `your-mysql-host.mysql.rds.aliyuncs.com`
- **端口**: `3306`
- **数据库名**: `music_service`
- **用户名**: `your_username`
- **密码**: `your_password`

### 2. 部署音乐服务到阿里云函数计算FC

#### 2.1 打包项目

```bash
cd d:\girlfrend\music-service
mvn clean package -DskipTests
```

生成的jar包位置：`d:\girlfrend\music-service\target\music-service-1.0.0.jar`

#### 2.2 创建函数计算应用

1. 登录阿里云控制台
2. 进入函数计算FC
3. 创建新应用/函数
4. 选择Java 17运行时
5. 上传 `music-service-1.0.0.jar`

#### 2.3 配置环境变量

在函数计算控制台添加以下环境变量：

| 变量名 | 值 | 说明 |
|--------|-----|------|
| `MYSQL_URL` | `jdbc:mysql://your-mysql-host:3306/music_service?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true` | MySQL连接URL |
| `MYSQL_USERNAME` | `your_username` | 数据库用户名 |
| `MYSQL_PASSWORD` | `your_password` | 数据库密码 |
| `MUSIC_STORAGE_PATH` | `/tmp/music-files` | 临时存储路径（函数计算环境） |
| `CORS_ALLOWED_ORIGINS` | `*` | 允许的跨域来源 |

#### 2.4 配置HTTP触发器

1. 创建HTTP触发器
2. 设置路由为 `/*`
3. 记录函数访问URL（类似 `https://xxx.fcapp.run`）

### 3. 更新情侣网站前端配置

#### 3.1 更新环境变量

编辑 `frontend/.env.production`：

```bash
VITE_API_BASE_URL=https://your-aliyun-fc-url.fcapp.run
VITE_MUSIC_SERVICE_URL=https://your-music-service-url.fcapp.run
```

将 `your-music-service-url` 替换为第2.4步获得的函数访问URL。

#### 3.2 重新构建并部署前端

```bash
cd d:\girlfrend\frontend
npm run build
```

然后将 `dist/` 目录推送到Vercel。

### 4. 上传音乐文件

#### 4.1 访问管理后台

打开浏览器访问：`https://your-music-service-url.fcapp.run`

#### 4.2 上传音乐

1. 点击"上传音乐"区域
2. 拖拽或选择音乐文件
3. 填写音乐名称和歌手
4. 点击"上传音乐"按钮

#### 4.3 批量上传

点击"批量上传"按钮，可同时选择多个音乐文件。

### 5. 验证部署

#### 5.1 测试音乐服务API

```bash
# 获取音乐列表
curl https://your-music-service-url.fcapp.run/api/music/list

# 测试播放（会重定向到实际音乐URL）
curl -I https://your-music-service-url.fcapp.run/api/music/stream/xxx.mp3
```

#### 5.2 测试情侣网站

1. 打开情侣网站
2. 点击页面触发用户交互
3. 检查音乐是否正常播放

## 🔧 常见问题

### Q1: 音乐文件存储在哪里？

**A**: 在函数计算环境中，文件存储在 `/tmp/music-files` 目录。注意：函数计算的临时存储空间有限，如果上传大量音乐，建议使用OSS对象存储。

### Q2: 如何扩容存储？

**A**: 如果需要存储大量音乐文件，建议：
1. 使用阿里云OSS存储音乐文件
2. 修改 `MusicController.java` 中的文件上传逻辑
3. 将文件URL改为OSS的公开访问URL

### Q3: CORS跨域问题？

**A**: 确保 `CORS_ALLOWED_ORIGINS` 环境变量设置正确。如果需要限制特定域名，可设置为：
```
CORS_ALLOWED_ORIGINS=https://your-domain.com,https://www.your-domain.com
```

### Q4: 数据库连接失败？

**A**: 检查：
1. MySQL服务器是否运行正常
2. 数据库连接信息是否正确
3. 函数计算是否能访问MySQL服务器（网络配置）
4. 数据库用户权限是否正确

## 📝 API接口文档

### 获取音乐列表

```
GET /api/music/list
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "晴天",
      "artist": "周杰伦",
      "fileName": "abc123.mp3",
      "fileUrl": "https://xxx.fcapp.run/api/music/stream/abc123.mp3",
      "fileSize": 5242880,
      "createTime": "2024-01-01T12:00:00"
    }
  ]
}
```

### 上传音乐

```
POST /api/music/upload
Content-Type: multipart/form-data

file: [音乐文件]
name: 歌曲名称
artist: 歌手名称
description: 描述（可选）
```

### 播放音乐

```
GET /api/music/stream/{fileName}
```

直接返回音频流，可用于 `<audio>` 标签播放。

### 删除音乐

```
DELETE /api/music/{id}
```

## 🔒 安全建议

1. **限制CORS**: 设置具体的允许域名，而不是 `*`
2. **文件大小限制**: 调整 `max-file-size` 防止上传过大文件
3. **访问控制**: 如需限制访问，可添加身份验证
4. **定期备份**: 定期备份MySQL数据库和音乐文件

## 💰 费用说明

- **函数计算FC**: 有免费额度，超出后按量计费
- **MySQL数据库**: 如果使用云数据库，按规格计费
- **存储空间**: 函数计算临时存储有限，大量音乐建议使用OSS
