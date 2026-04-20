# 🚀 Vercel 前端部署指南

## 📋 前提条件

- ✅ 已有 GitHub 账号
- ✅ 后端已部署到阿里云 FC（获取到 FC URL）

---

## 🎯 部署步骤

### 步骤 1：访问 Vercel

1. 打开 https://vercel.com
2. 点击 **"Sign Up"** 或 **"Login"**
3. 选择 **"Continue with GitHub"**（使用 GitHub 登录）

### 步骤 2：导入项目

1. 登录后，点击 **"Add New..."** → **"Project"**
2. 在 **"Import Git Repository"** 页面
3. 找到 `xiaobaiba999/yaoyao_home` 仓库
4. 点击 **"Import"**

### 步骤 3：配置项目

#### Root Directory（重要！）

点击 **"Edit"**，输入：
```
frontend
```

**⚠️ 必须设置！因为前端代码在 frontend 目录下**

#### Framework Preset

选择：
```
Vite
```

### 步骤 4：配置环境变量

点击 **"Environment Variables"**，添加以下变量：

#### 开发环境（Development）

```
VITE_API_BASE_URL=http://localhost:3000
```

#### 预览环境（Preview）

```
VITE_API_BASE_URL=https://your-backend-url.fcapp.run
```

#### 生产环境（Production）

```
VITE_API_BASE_URL=https://your-backend-url.fcapp.run
```

**⚠️ 重要：将 `your-backend-url` 替换为您实际的阿里云 FC URL**

例如：
```
VITE_API_BASE_URL=https://fc-12345678.cn-shanghai.fcapp.run
```

### 步骤 5：部署

1. 点击 **"Deploy"**
2. 等待部署完成（约 2-3 分钟）
3. 部署成功后，会显示：
   ```
   https://yaoyao-home-xxxx.vercel.app
   ```

---

## 🎊 恭喜！部署完成！

### 您的网站地址：

```
https://yaoyao-home-xxxx.vercel.app
```

### 登录测试：

- 用户名：`yaoyao`
- 密码：`yaoyao20230228`

---

## 🔧 自定义域名（可选）

### 步骤 1：购买域名

在阿里云、腾讯云、Namecheap 等平台购买域名，例如：
```
yaoyao-love.com
```

### 步骤 2：配置 Vercel

1. 进入 Vercel 项目
2. 点击 **"Settings"** → **"Domains"**
3. 输入您的域名：`yaoyao-love.com`
4. 点击 **"Add"**

### 步骤 3：配置 DNS

在域名注册商处添加 DNS 记录：

```
类型：CNAME
名称：@ 或 www
值：cname.vercel-dns.com
```

### 步骤 4：等待生效

DNS 生效需要 **10 分钟 - 24 小时**

生效后访问：
```
https://yaoyao-love.com
```

---

## 📊 更新部署

### 自动部署

每次推送到 GitHub 后，Vercel 会自动部署：

```bash
git add .
git commit -m "feat: 新功能"
git push origin main
```

Vercel 会在 **1-2 分钟** 内自动完成部署！

### 手动重新部署

1. 进入 Vercel 项目
2. 点击 **"Deployments"**
3. 找到最新的部署
4. 点击 **"⋮"** → **"Redeploy"**

---

## 🔍 常见问题

### Q1: 页面空白或无法加载

**解决：**
1. 打开浏览器控制台（F12）
2. 查看 Network 标签
3. 检查 API 请求是否成功
4. 确认环境变量 `VITE_API_BASE_URL` 配置正确

### Q2: 跨域错误（CORS）

**解决：**
1. 检查后端环境变量：
   ```
   CORS_ALLOWED_ORIGINS=https://yaoyao-home-xxxx.vercel.app
   ```
2. 或者设置为 `*` 允许所有域名（开发环境）

### Q3: 登录失败

**检查：**
1. 后端 FC 服务是否正常运行
2. 数据库连接是否正常
3. 前端环境变量中的后端 URL 是否正确

---

## 💰 费用说明

**Vercel 免费计划包含：**
- ✅ 无限次部署
- ✅ 100 GB 带宽/月
- ✅ 自动 SSL 证书
- ✅ 全球 CDN 加速
- ✅ 自动域名（.vercel.app）

**情侣网站预估用量：**
- 带宽：约 1-5 GB/月
- 部署次数：约 10-20 次/月

**结论：完全在免费额度内！✅**

---

## 🎯 完整部署流程总结

1. ✅ **创建 TiDB Cloud 数据库**（已完成）
2. ✅ **导入表结构**（已完成）
3. ✅ **打包后端项目**（已完成）
4. ⏳ **部署后端到阿里云 FC**
5. ⏳ **部署前端到 Vercel**
6. ⏳ **测试登录和功能**

---

## 🎊 部署完成检查清单

- [ ] 后端部署到阿里云 FC ✅
- [ ] 获取后端 FC URL ✅
- [ ] 前端部署到 Vercel ✅
- [ ] 配置环境变量 VITE_API_BASE_URL ✅
- [ ] 访问 Vercel 域名测试 ✅
- [ ] 登录功能测试 ✅
- [ ] 照片上传测试 ✅
- [ ] 音乐播放测试 ✅
- [ ] （可选）配置自定义域名 ✅

---

## 📝 需要帮助？

- Vercel 文档：https://vercel.com/docs
- Vercel 中文文档：https://vercel.com/docs/concepts
- Vercel CLI：https://vercel.com/docs/cli

---

**祝您的情侣网站运行顺利！❤️**
