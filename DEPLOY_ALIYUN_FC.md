# 🚀 阿里云函数计算 FC 部署指南

## 📋 准备工作

1. **登录阿里云**：https://www.aliyun.com
2. **开通函数计算 FC**：搜索"函数计算"并开通（按量付费，有免费额度）

---

## 🎯 方式一：使用控制台手动部署（推荐新手）

### 步骤 1：创建函数

1. 进入 **函数计算控制台**：https://fcnext.console.aliyun.com/
2. 点击 **"创建函数"**
3. 选择 **"从零开始创建"**

### 步骤 2：配置函数基本信息

```
函数名称：yaoyao-backend
运行环境：Java 11
内存：512 MB
超时时间：30 秒
```

### 步骤 3：上传代码

1. 选择 **"上传 ZIP 包"**
2. 打包 JAR 文件：
   ```bash
   # 在 backend 目录执行
   cd target
   zip -r yaoyao-backend.zip yaoyao-memorial-3.0.0.jar
   ```
3. 上传 `yaoyao-backend.zip`

### 步骤 4：配置触发器

1. 点击 **"添加触发器"**
2. 选择 **"HTTP 触发器"**
3. 配置：
   ```
   触发器名称：http-trigger
   请求方法：ANY（支持所有方法）
   认证方式：无需认证（应用层自己做 JWT 认证）
   ```

### 步骤 5：配置环境变量

在函数配置的 **"环境变量"** 中添加：

```
SPRING_PROFILES_ACTIVE=production
DB_HOST=gateway01.ap-northeast-1.prod.aws.tidbcloud.com
DB_PORT=4000
DB_NAME=yaoyao_memorial
DB_USER=3K7wRbrKq3XuNXn.root
DB_PASSWORD=VnVOOxo90nYBkq5V
JWT_SECRET=yaoyao-memorial-secret-key-2024-production
ADMIN_USERNAME=yaoyao
ADMIN_PASSWORD=yaoyao20230228
CORS_ALLOWED_ORIGINS=*
```

### 步骤 6：完成创建

点击 **"完成"**，等待部署（约 2-3 分钟）

### 步骤 7：获取访问地址

部署成功后，您会看到：
```
https://xxxxx-xxxxx.cn-shanghai.fcapp.run
```

**🎉 保存这个 URL！后面配置前端需要用到！**

---

## 🎯 方式二：使用 Serverless Devs 工具（推荐高级用户）

### 安装 Serverless Devs

```bash
npm install -g @serverless-devs/s
```

### 配置阿里云凭据

```bash
s config add
# 输入您的 AccessKey ID 和 AccessKey Secret
```

### 创建 s.yaml 配置文件

在项目根目录创建 `s.yaml`：

```yaml
edition: 1.0.0
name: yaoyao-backend
access: default

vars:
  region: cn-shanghai
  functionName: yaoyao-backend
  runtime: java11
  memorySize: 512
  timeout: 30

services:
  yaoyao-backend:
    component: fc
    props:
      region: ${vars.region}
      functionName: ${vars.functionName}
      description: 情侣网站后端服务
      runtime: ${vars.runtime}
      memorySize: ${vars.memorySize}
      timeout: ${vars.timeout}
      handler: App::handleRequest
      codeUri: ./backend/target/yaoyao-memorial-3.0.0.jar
      layers:
        - acs:fc:${vars.region}:official:layers/Java11/versions/1
      environmentVariables:
        SPRING_PROFILES_ACTIVE: production
        DB_HOST: gateway01.ap-northeast-1.prod.aws.tidbcloud.com
        DB_PORT: 4000
        DB_NAME: yaoyao_memorial
        DB_USER: 3K7wRbrKq3XuNXn.root
        DB_PASSWORD: VnVOOxo90nYBkq5V
        JWT_SECRET: yaoyao-memorial-secret-key-2024-production
        ADMIN_USERNAME: yaoyao
        ADMIN_PASSWORD: yaoyao20230228
        CORS_ALLOWED_ORIGINS: '*'
      triggers:
        - triggerName: httpTrigger
          triggerType: http
          triggerConfig:
            methods:
              - GET
              - POST
              - PUT
              - DELETE
              - PATCH
              - HEAD
              - OPTIONS
            authType: anonymous
```

### 部署

```bash
s deploy
```

---

## ✅ 验证部署

### 测试登录接口

```bash
curl -X POST https://your-fc-url.fcapp.run/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"yaoyao","password":"yaoyao20230228"}'
```

**预期返回：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "user": {...}
  }
}
```

---

## 🔧 常见问题

### Q1: 函数启动失败

**解决：**
1. 检查环境变量配置是否正确
2. 查看函数日志（控制台 → 函数 → 日志查询）
3. 确认数据库连接信息正确

### Q2: 跨域问题

**解决：**
确保环境变量中配置了：
```
CORS_ALLOWED_ORIGINS=*
```

### Q3: 数据库连接失败

**检查：**
1. TiDB Cloud 是否允许公网访问
2. 用户名密码是否正确
3. 数据库名是否为 `yaoyao_memorial`

---

## 💰 费用说明

**阿里云 FC 免费额度：**
- 每月 400,000 GB·s 资源使用量
- 每月 1,000,000 次请求
- 每月 100 GB 外网出流量

**情侣网站预估用量：**
- 内存：512 MB
- 运行时间：约 100 小时/月
- 用量：512 MB × 100 小时 × 3600 秒 = **184,320,000 GB·s**

**结论：完全在免费额度内！✅**

---

## 🎊 部署完成！

您的后端服务已经部署到阿里云函数计算！

**下一步：部署前端到 Vercel**

请继续查看 Vercel 部署指南！
