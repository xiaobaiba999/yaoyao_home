# 音乐功能测试指南

## 功能概述
已成功实现从歌曲海（gequhai.com）搜索和播放音乐的功能，无需将音乐文件存储在项目中。

## 已完成的工作

### 1. 后端服务
- ✅ 创建 `MusicSearchService` 接口定义音乐搜索功能
- ✅ 实现 `GequhaiMusicServiceImpl` 代理爬取歌曲海API
- ✅ 创建 `MusicController` 提供REST API端点
- ✅ 配置CORS允许跨域访问
- ✅ 将 `/api/music/**` 添加到公开访问路径（无需登录）

### 2. 前端界面
- ✅ 重构 `MusicPlayer.vue` 组件，新增：
  - 📑 **标签页切换**：播放列表 / 搜索
  - 🔍 **搜索界面**：输入关键词搜索歌曲
  - ➕ **添加功能**：一键添加到播放列表
  - 🎵 **预览播放**：点击即可试听
  - ✏️ **手动添加**：支持自定义URL添加音乐
  - 🗑️ **删除管理**：移除不需要的歌曲

### 3. 状态管理优化
- ✅ 更新 `bgm.js` Store，新增 `isTrackInPlaylist()` 方法
- ✅ 防止重复添加同一首歌曲
- ✅ 优化播放逻辑，避免重复创建音轨

## API 端点

### 搜索音乐
```
GET /api/music/search?keyword=周杰伦
```

**响应示例：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "12345",
      "name": "晴天",
      "artist": "周杰伦",
      "album": "叶惠美",
      "duration": "04:29",
      "url": "https://cdn.gequhai.com/mp3/12345.mp3",
      "cover": "https://cdn.gequhai.com/cover/12345.jpg"
    }
  ]
}
```

### 获取播放URL
```
GET /api/music/url/{songId}
```

## 使用方法

### 方法一：通过UI界面操作（推荐）
1. 打开网站，点击右下角的音乐图标🎵
2. 点击"🔍 搜索"标签页
3. 在搜索框输入歌名或歌手名（如"周杰伦"、"晴天"）
4. 点击"搜索"按钮或按回车键
5. 浏览搜索结果：
   - **点击歌曲信息** → 立即预览播放
   - **点击"+"按钮** → 添加到播放列表
6. 切换到"播放列表"标签查看已添加的歌曲
7. 使用播放控制按钮控制播放

### 方法二：手动添加音乐链接
1. 点击播放列表中的"+ 添加"按钮
2. 输入歌曲名称和有效的MP3 URL
3. 点击"添加"确认

## 技术特点

### 优势
- ✨ **零存储成本**：音乐文件托管在外部CDN，不占用服务器空间
- ⚡ **快速加载**：使用CDN加速，播放流畅
- 🔒 **安全代理**：后端代理请求，避免前端直接跨域问题
- 💾 **本地缓存**：播放列表保存在localStorage，刷新不丢失
- 🎯 **智能去重**：自动检测重复歌曲，避免重复添加
- 📱 **响应式设计**：完美适配移动端和桌面端

### 支持的功能
- 歌曲搜索（按歌名、歌手）
- 即时预览播放
- 批量添加到播放列表
- 自定义音乐URL
- 播放列表管理（增删改查）
- 音量调节
- 上一首/下一首切换
- 滚动时自动最小化

## 注意事项

1. **网络要求**：需要稳定的互联网连接以访问外部音乐资源
2. **版权声明**：音乐来源于第三方平台，仅供个人学习使用
3. **可用性**：依赖外部API的稳定性，如遇问题可尝试手动添加URL
4. **浏览器兼容性**：支持现代浏览器（Chrome、Firefox、Safari、Edge）

## 故障排除

### 问题：搜索无结果
- 检查网络连接
- 尝试不同的关键词
- 确认后端服务正在运行

### 问题：播放失败
- 检查浏览器控制台是否有错误
- 尝试其他歌曲
- 手动添加其他音乐源

### 问题：401未授权错误
- 已修复：音乐API现已公开访问，无需登录

## 后续优化建议

1. [ ] 添加歌词显示功能
2. [ ] 支持多个音乐源切换（网易云、QQ音乐等）
3. [ ] 添加下载功能（如果版权允许）
4. [ ] 实现播放历史记录
5. [ ] 支持创建多个播放列表
6. [ ] 添加歌曲收藏功能

## 相关文件

### 后端
- `backend/src/main/java/com/yaoyao/service/MusicSearchService.java`
- `backend/src/main/java/com/yaoyao/service/impl/GequhaiMusicServiceImpl.java`
- `backend/src/main/java/com/yaoyao/controller/MusicController.java`
- `backend/src/main/java/com/yaoyao/config/WebMvcConfig.java`

### 前端
- `frontend/src/components/MusicPlayer.vue`
- `frontend/src/stores/bgm.js`

---

**更新时间**: 2026-04-20
**状态**: ✅ 已完成并可使用
