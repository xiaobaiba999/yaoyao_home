package com.yaoyao.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yaoyao.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/music")
@RequiredArgsConstructor
public class MusicController {

    private final Map<String, MusicUrlCache> urlCache = new ConcurrentHashMap<>();

    // 内置音乐列表 - 使用网易云音乐ID
    private static final List<Map<String, String>> builtinMusicList = List.of(
        Map.of("id", "m1", "name", "晴天", "artist", "周杰伦", "neteaseId", "28490004"),
        Map.of("id", "m2", "name", "稻香", "artist", "周杰伦", "neteaseId", "108726"),
        Map.of("id", "m3", "name", "花海", "artist", "周杰伦", "neteaseId", "108725"),
        Map.of("id", "m4", "name", "发如雪", "artist", "周杰伦", "neteaseId", "108723"),
        Map.of("id", "m5", "name", "反方向的钟", "artist", "周杰伦", "neteaseId", "108721"),
        Map.of("id", "m6", "name", "给我一首歌的时间", "artist", "周杰伦", "neteaseId", "6471268"),
        Map.of("id", "m7", "name", "明明就", "artist", "周杰伦", "neteaseId", "25717770"),
        Map.of("id", "m8", "name", "蒲公英的约定", "artist", "周杰伦", "neteaseId", "108729"),
        Map.of("id", "m9", "name", "一路向北", "artist", "周杰伦", "neteaseId", "108732"),
        Map.of("id", "m10", "name", "修炼爱情", "artist", "林俊杰", "neteaseId", "26463519"),
        Map.of("id", "m11", "name", "有何不可", "artist", "许嵩", "neteaseId", "274728"),
        Map.of("id", "m12", "name", "清明雨上", "artist", "许嵩", "neteaseId", "274727"),
        Map.of("id", "m13", "name", "泡沫", "artist", "邓紫棋", "neteaseId", "27572558"),
        Map.of("id", "m14", "name", "绿光", "artist", "孙燕姿", "neteaseId", "185807"),
        Map.of("id", "m15", "name", "开始懂了", "artist", "孙燕姿", "neteaseId", "185805"),
        Map.of("id", "m16", "name", "我怀念的", "artist", "孙燕姿", "neteaseId", "185816"),
        Map.of("id", "m17", "name", "遇见", "artist", "孙燕姿", "neteaseId", "185813"),
        Map.of("id", "m18", "name", "爱丫爱丫", "artist", "BY2", "neteaseId", "27759672"),
        Map.of("id", "m19", "name", "倒数", "artist", "邓紫棋", "neteaseId", "1295966555"),
        Map.of("id", "m20", "name", "唯一", "artist", "王力宏", "neteaseId", "216972"),
        Map.of("id", "m21", "name", "再见", "artist", "邓紫棋", "neteaseId", "38597394"),
        Map.of("id", "m22", "name", "过活", "artist", "棉子", "neteaseId", "1349394945"),
        Map.of("id", "m23", "name", "Always Online", "artist", "林俊杰", "neteaseId", "26463525")
    );

    // 音乐URL缓存类，包含URL和过期时间
    private static class MusicUrlCache {
        String url;
        long expireTime;

        MusicUrlCache(String url, long expireTime) {
            this.url = url;
            this.expireTime = expireTime;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }

    @GetMapping("/list")
    public Result<List<Map<String, String>>> list() {
        List<Map<String, String>> musicList = builtinMusicList.stream()
            .map(m -> Map.of(
                "id", m.get("id"),
                "name", m.get("name"),
                "artist", m.get("artist")
            ))
            .toList();
        return Result.success(musicList);
    }

    @GetMapping("/play/{id}")
    public void play(@PathVariable String id, HttpServletResponse response) {
        try {
            var music = builtinMusicList.stream()
                .filter(m -> m.get("id").equals(id))
                .findFirst();

            if (music.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("Music not found");
                return;
            }

            String neteaseId = music.get().get("neteaseId");
            String cacheKey = id;

            // 检查缓存
            MusicUrlCache cache = urlCache.get(cacheKey);
            String playUrl = null;

            if (cache != null && !cache.isExpired()) {
                playUrl = cache.url;
            } else {
                // 获取音乐URL
                playUrl = getNeteaseMusicUrl(neteaseId);
                if (playUrl != null && !playUrl.isEmpty()) {
                    // 缓存1小时
                    urlCache.put(cacheKey, new MusicUrlCache(playUrl, System.currentTimeMillis() + 3600000));
                }
            }

            if (playUrl == null || playUrl.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("Music URL not available");
                return;
            }

            // 转发请求到实际的音乐URL，并将响应流写回客户端
            streamMusicToClient(playUrl, response);

        } catch (Exception e) {
            System.err.println("音乐播放失败: " + e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取网易云音乐真实播放链接
     */
    private String getNeteaseMusicUrl(String neteaseId) {
        try {
            // 使用多个API尝试获取链接
            String[] apis = {
                "https://api.injahow.cn/song/url?id=" + neteaseId + "&br=128000",
                "https://music.163.com/song/media/outer/url?id=" + neteaseId + ".mp3"
            };

            for (String api : apis) {
                try {
                    HttpResponse httpResponse = HttpRequest.get(api)
                        .header("Referer", "https://music.163.com/")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .timeout(15000)
                        .execute();

                    // 如果是重定向
                    if (httpResponse.getStatus() == 302 || httpResponse.getStatus() == 301) {
                        String location = httpResponse.header("Location");
                        if (location != null && !location.isEmpty()) {
                            System.out.println("获取到重定向URL: " + location);
                            return location;
                        }
                    }

                    String body = httpResponse.body();
                    if (body == null || body.isEmpty()) {
                        continue;
                    }

                    // 解析JSON响应
                    try {
                        JSONObject json = JSONUtil.parseObj(body);
                        if (json.getInt("code", -1) == 200) {
                            JSONArray songs = json.getJSONArray("songs");
                            if (songs != null && !songs.isEmpty()) {
                                String url = songs.getJSONObject(0).getStr("url");
                                if (url != null && !url.isEmpty()) {
                                    System.out.println("从API获取到URL: " + url);
                                    return url;
                                }
                            }
                        } else {
                            System.out.println("API返回错误码: " + json.getInt("code", -1));
                        }
                    } catch (Exception e) {
                        // 不是JSON格式，跳过
                        System.out.println("非JSON响应，跳过");
                    }
                } catch (Exception e) {
                    System.out.println("API请求失败: " + api + " - " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("获取音乐链接失败: " + e.getMessage());
        }
        return null;
    }

    /**
     * 将音乐流转发给客户端
     */
    private void streamMusicToClient(String musicUrl, HttpServletResponse response) {
        HttpResponse musicResponse = null;
        try {
            musicResponse = HttpRequest.get(musicUrl)
                .header("Referer", "https://music.163.com/")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .timeout(30000)
                .execute();

            int statusCode = musicResponse.getStatus();
            System.out.println("音乐URL响应状态: " + statusCode);

            if (statusCode == 302 || statusCode == 301) {
                // 再次重定向
                String location = musicResponse.header("Location");
                if (location != null) {
                    System.out.println("二次重定向到: " + location);
                    response.sendRedirect(location);
                    return;
                }
            }

            if (statusCode != 200) {
                System.err.println("音乐URL返回非200状态: " + statusCode);
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("Music not available");
                return;
            }

            response.setContentType("audio/mpeg");
            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Accept-Ranges", "bytes");
            response.setHeader("Cache-Control", "public, max-age=3600");

            // 获取内容长度
            long contentLength = musicResponse.contentLength();
            if (contentLength > 0) {
                response.setContentLengthLong(contentLength);
            }

            // 流式传输
            try (InputStream is = musicResponse.bodyStream();
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int length;
                while ((length = is.read(buffer)) != -1) {
                    os.write(buffer, 0, length);
                    os.flush();
                }
            }

        } catch (Exception e) {
            System.err.println("转发音乐流失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (musicResponse != null) {
                musicResponse.close();
            }
        }
    }
}
