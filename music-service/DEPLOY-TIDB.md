# 音乐服务线上部署指南（TiDB Cloud 版本）

## 📦 项目结构

```
girlfrend/
├── frontend/           # 情侣网站前端（Vercel）
├── backend/            # 情侣网站后端（阿里云函数计算FC）
├── music-service/      # 音乐服务（部署到阿里云函数计算FC）
└── music-files/        # 音乐文件存储目录（临时）
```

## 🚀 部署步骤

### 第1步：在 TiDB Cloud 创建数据库

#### 1.1 登录 TiDB Cloud

访问：https://tidbcloud.com/

#### 1.2 创建新集群

1. 点击 "New Cluster"
2. 选择 "Serverless" 套餐（免费）
3. 填写集群名称：`music-service`
4. 选择区域：选择离你用户最近的区域（建议：AWS Asia Pacific (Tokyo)）
5. 点击 "Create"

#### 1.3 获取数据库连接信息

1. 在新创建的集群页面，点击 "Connect"
2. 选择 "General Connection String"
3. 复制连接字符串，格式类似：
   ```
   mysql -h xxxxxx.ap-northeast-1.prod.aws.tidbcloud.com -P 4000 -u xxxxx.root -p
   ```
4. 设置或重置密码

#### 1.4 创建数据库

在 TiDB Cloud 控制台或本地执行：
```sql
CREATE DATABASE IF NOT EXISTS music_service;
```

**记录以下信息**（后续需要）：
- **Host**: `xxxxxx.ap-northeast-1.prod.aws.tidbcloud.com`
- **Port**: `4000`
- **Username**: `xxxxx.root`
- **Password**: `your_password`

### 第2步：部署音乐服务到阿里云函数计算FC

#### 2.1 打包项目

```bash
cd d:\girlfrend\music-service
mvn clean package -DskipTests
```

生成的jar包位置：`d:\girlfrend\music-service\target\music-service-1.0.0.jar`

#### 2.2 登录阿里云函数计算

访问：https://fcnext.console.aliyun.com/

#### 2.3 创建应用

1. 点击 "创建应用"
2. 选择 "事件函数" 或 "HTTP 函数"
3. 运行环境选择：**Java 17**
4. 上传方式：选择 "上传JAR包"
5. 上传 `music-service-1.0.0.jar`

#### 2.4 配置环境变量（重要！）

在函数配置页面，添加以下环境变量：

| 变量名 | 值 | 示例 |
|--------|-----|------|
| `MYSQL_URL` | TiDB Cloud 连接URL | `jdbc:mysql://xxxxxx.ap-northeast-1.prod.aws.tidbcloud.com:4000/music_service?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true` |
| `MYSQL_USERNAME` | TiDB 用户名 | `xxxxx.root` |
| `MYSQL_PASSWORD` | TiDB 密码 | `your_password` |
| `MUSIC_STORAGE_PATH` | 存储路径 | `/tmp/music-files` |
| `CORS_ALLOWED_ORIGINS` | 允许的域名 | `*` |

**MYSQL_URL 完整格式**：
```
jdbc:mysql://[HOST]:4000/music_service?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
```

#### 2.5 配置 HTTP 触发器

1. 在函数页面，点击 "触发器" 标签
2. 点击 "创建触发器"
3. 类型选择 "HTTP"
4. 请求方法选择 "ANY"
5. 路径配置为 `/*`
6. 点击 "确定"

7. **记录函数访问URL**（重要！）：`https://xxx.fcapp.run`

### 第3步：更新情侣网站前端配置

#### 3.1 更新环境变量

编辑 `frontend/.env.production`：

```bash
VITE_API_BASE_URL=https://your-aliyun-fc-url.fcapp.run
VITE_MUSIC_SERVICE_URL=https://your-music-service-url.fcapp.run
```

将 `your-music-service-url` 替换为第 2.5 步获得的函数 URL。

#### 3.2 重新构建并部署前端

```bash
cd d:\girlfrend\frontend
npm run build
```

然后将 `dist/` 目录推送到 Vercel。

### 第4步：上传音乐文件

#### 4.1 访问管理后台

打开浏览器访问：`https://your-music-service-url.fcapp.run`

#### 4.2 上传音乐

1. 拖拽音乐文件到上传区域
2. 填写音乐名称和歌手
3. 点击"上传音乐"

#### 4.3 批量上传

点击"批量上传"按钮，可同时上传多个文件。

### 第5步：验证部署

#### 5.1 测试音乐服务API

```bash
# 获取音乐列表
curl https://your-music-service-url.fcapp.run/api/music/list

# 测试播放
curl -I https://your-music-service-url.fcapp.run/api/music/stream/xxx.mp3
```

#### 5.2 测试情侣网站

1. 打开情侣网站
2. 点击页面触发用户交互
3. 检查音乐是否正常播放

## 🔧 常见问题

### Q1: TiDB Cloud 连接失败？

**A**: 检查：
1. 确保 TiDB 集群状态为 "Active"
2. 检查用户名密码是否正确
3. 确保函数计算能访问公网（TiDB Cloud 在 AWS）
4. 检查 TiDB Cloud 的访问控制设置

### Q2: 音乐文件会丢失吗？

**A**: 函数计算的 `/tmp` 目录是临时的，函数重启后文件会丢失。
**解决方案**：
- 少量测试：可以暂时使用
- 生产环境：建议使用阿里云 OSS 存储音乐文件

### Q3: 如何迁移到 OSS 存储？

**A**: 
1. 创建阿里云 OSS Bucket
2. 修改 `MusicController.java` 中的上传逻辑
3. 将文件上传到 OSS 而不是本地
4. 返回 OSS 的公开访问 URL

### Q4: CORS 跨域问题？

**A**: 确保 `CORS_ALLOWED_ORIGINS` 环境变量设置正确。

## 📝 API 接口文档

### 获取音乐列表
```
GET /api/music/list
```

### 上传音乐
```
POST /api/music/upload
Content-Type: multipart/form-data

file: [音乐文件]
name: 歌曲名称
artist: 歌手名称
```

### 播放音乐
```
GET /api/music/stream/{fileName}
```

### 删除音乐
```
DELETE /api/music/{id}
```

## 💰 费用说明

- **TiDB Cloud Serverless**: 免费额度（10GB 存储 + 5000 万读单元/月）
- **阿里云函数计算FC**: 免费额度（每月一定次数的调用）
- **存储空间**: 函数计算临时存储有限，建议后续迁移到 OSS（约¥0.12/GB/月）

## 🎯 下一步优化建议

1. **迁移到 OSS 存储**：确保音乐文件持久化
2. **添加访问控制**：保护音乐上传接口
3. **配置 CDN**：加速音乐播放
4. **监控和日志**：使用阿里云 SLS 查看函数日志
