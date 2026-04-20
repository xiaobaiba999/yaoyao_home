# 🚀 情侣网站线上部署指南

本文档指导您如何将情侣网站部署到线上，包括：
- 前端 → Vercel
- 后端 → 阿里云函数计算FC
- 数据库 → 免费MySQL方案

---

## 📋 前提条件

- ✅ GitHub账号（已有）
- ✅ 阿里云账号
- ✅ Vercel账号（可用GitHub登录）

---

## 🗄️ 第一步：配置免费数据库

### 推荐方案1：TiDB Cloud（免费5GB）

**优点**：
- 永久免费 5GB 存储
- 兼容MySQL协议
- 全球CDN加速
- 无需信用卡

**注册步骤**：
1. 访问 https://tidbcloud.com/
2. 使用GitHub账号登录
3. 创建集群，选择区域（推荐 AWS Tokyo）
4. 获取连接信息：
   - Host: `xxxxxx.tidbcloud.com`
   - Port: `4000`
   - User: `xxxxxx.root`
   - Password: `您的密码`
   - Database: `yaoyao_memorial`

**导入数据库结构**：
```sql
CREATE DATABASE IF NOT EXISTS yaoyao_memorial 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE yaoyao_memorial;

-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    nickname VARCHAR(100),
    avatar VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 照片表
CREATE TABLE IF NOT EXISTS photos (
    id VARCHAR(36) PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    url TEXT NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 留言板表
CREATE TABLE IF NOT EXISTS messages (
    id VARCHAR(36) PRIMARY KEY,
    content TEXT NOT NULL,
    author VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 计划表
CREATE TABLE IF NOT EXISTS plans (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    target_date DATE,
    status VARCHAR(50) DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 时间轴表
CREATE TABLE IF NOT EXISTS timeline (
    id VARCHAR(36) PRIMARY KEY,
    date VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 心愿表
CREATE TABLE IF NOT EXISTS wishes (
    id VARCHAR(36) PRIMARY KEY,
    content VARCHAR(500) NOT NULL,
    completed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 日记表
CREATE TABLE IF NOT EXISTS diaries (
    id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    mood VARCHAR(50),
    date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 推荐方案2：PlanetScale（免费5GB）

**优点**：
- 免费 5GB 存储
- 自动备份
- Git式分支

**注册**：https://planetscale.com/

---

## ☁️ 第二步：部署后端到阿里云函数计算FC

### 2.1 开通函数计算

1. 登录阿里云控制台
2. 搜索"函数计算FC"
3. 开通服务（按量付费，免费额度足够）

### 2.2 创建函数

**方式A：使用Serverless Devs工具**

```bash
# 安装Serverless Devs
npm install -g @serverless-devs/s

# 配置阿里云凭据
s config add

# 进入后端目录
cd backend

# 初始化项目
s init

# 部署
s deploy
```

**方式B：控制台手动部署**

1. 进入函数计算控制台
2. 创建函数：
   - 运行时：Java 11
   - 内存：512MB
   - 超时时间：30秒
   - 触发器：HTTP触发器

3. 打包项目：
```bash
cd backend
mvn clean package -DskipTests
```

4. 上传JAR包到函数计算

### 2.3 配置环境变量

在函数计算控制台添加以下环境变量：

```
SPRING_PROFILES_ACTIVE=production
DB_HOST=你的TiDB Host
DB_PORT=4000
DB_NAME=yaoyao_memorial
DB_USER=你的TiDB User
DB_PASSWORD=你的TiDB Password
JWT_SECRET=yaoyao-memorial-secret-key-2024-production
ADMIN_USERNAME=yaoyao
ADMIN_PASSWORD=yaoyao20230228
GITEE_ACCESS_TOKEN=你的Gitee Token
GITEE_OWNER=你的Gitee用户名
GITEE_REPO=你的Gitee仓库名
```

### 2.4 获取函数URL

部署成功后，您会得到类似这样的URL：
```
https://xxxxx.fcapp.run
```

**重要**：记录这个URL，后面配置前端需要用到！

---

## 🌐 第三步：部署前端到Vercel

### 3.1 连接GitHub

1. 访问 https://vercel.com
2. 使用GitHub账号登录
3. 点击"New Project"
4. 选择 `yaoyao_home` 仓库
5. 点击"Import"

### 3.2 配置构建

**Root Directory**：设置为 `frontend`

**Build Command**：
```bash
npm run build
```

**Output Directory**：
```
dist
```

**Install Command**：
```bash
npm install
```

### 3.3 配置环境变量

在Vercel项目设置中添加环境变量：

```
VITE_API_BASE_URL=https://你的后端FCURL.fcapp.run
VITE_MUSIC_API_URL=https://你的音乐APIURL.fcapp.run
```

### 3.4 部署

点击"Deploy"，等待部署完成（约2-3分钟）

部署成功后，您会得到：
```
https://yaoyao-home.vercel.app
```

---

## 🎵 第四步：部署音乐API（可选）

如果您想保留音乐功能，需要将 music-api 项目也部署到云函数。

### 4.1 修改 music-api/server.js

添加对阿里云FC的支持：

```javascript
// 添加FC兼容的导出
if (process.env.FC_MODE) {
    module.exports = app;
} else {
    app.listen(PORT, () => {
        console.log(`音乐API服务运行在端口 ${PORT}`);
    });
}
```

### 4.2 部署到FC

1. 压缩 music-api 目录
2. 上传到新的函数计算实例
3. 配置环境变量
4. 获取URL并更新前端配置

---

## ✅ 第五步：验证部署

### 5.1 检查后端API

访问以下URL验证：
```
https://你的后端FCURL.fcapp.run/api/auth/login
```

### 5.2 检查前端

1. 访问 `https://yaoyao-home.vercel.app`
2. 使用默认账号登录：
   - 用户名：`yaoyao`
   - 密码：`yaoyao20230228`

### 5.3 检查音乐功能

1. 点击右下角🎵图标
2. 应该能看到播放列表
3. 尝试播放一首歌曲

---

## 🔧 常见问题

### Q1: 前端跨域问题

**解决**：确保后端配置了正确的CORS：

```yaml
# application-production.yml
yaoyao:
  cors:
    allowed-origins: https://yaoyao-home.vercel.app
```

### Q2: 数据库连接失败

**检查**：
1. 确认环境变量配置正确
2. 检查数据库是否允许外部访问
3. 查看函数计算日志

### Q3: 图片上传失败

**解决**：配置Gitee存储：
1. 创建Gitee仓库
2. 生成Gitee Token
3. 配置环境变量

---

## 📊 费用估算

| 服务 | 方案 | 费用 |
|------|------|------|
| **前端** | Vercel | ✅ 免费 |
| **后端** | 阿里云FC | ✅ 免费额度内 |
| **数据库** | TiDB Cloud | ✅ 免费5GB |
| **图片存储** | Gitee | ✅ 免费 |
| **音乐API** | 阿里云FC | ✅ 免费额度内 |
| **域名** | 默认域名 | ✅ 免费 |
| | 自定义域名 | ¥60/年起 |

**总计：¥0/月** 🎉

---

## 🎯 部署检查清单

- [ ] 注册TiDB Cloud并创建数据库
- [ ] 导入数据库表结构
- [ ] 部署后端到阿里云FC
- [ ] 配置后端环境变量
- [ ] 获取后端FC URL
- [ ] 部署前端到Vercel
- [ ] 配置前端环境变量
- [ ] 获取前端Vercel URL
- [ ] 测试登录功能
- [ ] 测试照片上传
- [ ] 测试音乐播放
- [ ] （可选）部署音乐API
- [ ] （可选）配置自定义域名

---

## 🎊 部署完成！

恭喜！您的情侣网站现在已经在线上运行了！

**您的网站地址**：
- 前端：`https://yaoyao-home.vercel.app`
- 后端API：`https://xxxxx.fcapp.run`

**默认管理员账号**：
- 用户名：`yaoyao`
- 密码：`yaoyao20230228`

---

## 📝 后续优化建议

1. **配置自定义域名**
   - 购买域名（约¥60/年）
   - 配置DNS解析到Vercel
   - 在Vercel添加自定义域名

2. **启用HTTPS**
   - Vercel自动提供HTTPS
   - 函数计算需配置自定义域名

3. **添加CDN**
   - 使用Vercel全球CDN
   - 图片通过Gitee CDN加速

4. **设置监控告警**
   - 阿里云FC日志监控
   - Vercel部署监控

5. **定期备份数据库**
   - TiDB Cloud自动备份
   - 也可手动导出SQL

---

## 💡 需要帮助？

如果部署过程中遇到问题，请查看：
- 阿里云FC文档：https://help.aliyun.com/product/50980.html
- Vercel文档：https://vercel.com/docs
- TiDB Cloud文档：https://docs.pingcap.com/tidbcloud/

---

**祝您的情侣网站运行顺利！❤️**
