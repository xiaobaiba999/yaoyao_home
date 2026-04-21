package com.yaoyao.controller;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yaoyao.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/music")
@RequiredArgsConstructor
public class MusicController {

    private static final String GITEE_OWNER = "zhao-zhao-lu123";
    private static final String GITEE_REPO = "yaoyao-music";
    private static final String GITEE_BRANCH = "master";

    private static final List<Map<String, String>> builtinMusicList = List.of(
        Map.of("id", "m1", "name", "晴天", "artist", "周杰伦", "filename", "晴天.mp3"),
        Map.of("id", "m2", "name", "稻香", "artist", "周杰伦", "filename", "稻香.mp3"),
        Map.of("id", "m3", "name", "花海", "artist", "周杰伦", "filename", "花海.mp3"),
        Map.of("id", "m4", "name", "发如雪", "artist", "周杰伦", "filename", "发如雪.mp3"),
        Map.of("id", "m5", "name", "反方向的钟", "artist", "周杰伦", "filename", "反方向的钟.mp3"),
        Map.of("id", "m6", "name", "给我一首歌的时间", "artist", "周杰伦", "filename", "给我一首歌的时间.mp3"),
        Map.of("id", "m7", "name", "明明就", "artist", "周杰伦", "filename", "明明就.mp3"),
        Map.of("id", "m8", "name", "蒲公英的约定", "artist", "周杰伦", "filename", "蒲公英的约定.mp3"),
        Map.of("id", "m9", "name", "一路向北", "artist", "周杰伦", "filename", "一路向北.mp3"),
        Map.of("id", "m10", "name", "修炼爱情", "artist", "林俊杰", "filename", "修炼爱情.mp3"),
        Map.of("id", "m11", "name", "有何不可", "artist", "许嵩", "filename", "有何不可.mp3"),
        Map.of("id", "m12", "name", "清明雨上", "artist", "许嵩", "filename", "清明雨上.mp3"),
        Map.of("id", "m13", "name", "泡沫", "artist", "邓紫棋", "filename", "泡沫.mp3"),
        Map.of("id", "m14", "name", "绿光", "artist", "孙燕姿", "filename", "绿光.mp3"),
        Map.of("id", "m15", "name", "开始懂了", "artist", "孙燕姿", "filename", "开始懂了.mp3"),
        Map.of("id", "m16", "name", "我怀念的", "artist", "孙燕姿", "filename", "我怀念的.mp3"),
        Map.of("id", "m17", "name", "遇见", "artist", "孙燕姿", "filename", "遇见.mp3"),
        Map.of("id", "m18", "name", "爱丫爱丫", "artist", "BY2", "filename", "爱丫爱丫.mp3"),
        Map.of("id", "m19", "name", "倒数", "artist", "邓紫棋", "filename", "倒数.mp3"),
        Map.of("id", "m20", "name", "唯一", "artist", "王力宏", "filename", "唯一.mp3"),
        Map.of("id", "m21", "name", "再见", "artist", "邓紫棋", "filename", "再见.mp3"),
        Map.of("id", "m22", "name", "过活", "artist", "棉子", "filename", "过活.mp3"),
        Map.of("id", "m23", "name", "Aways online", "artist", "林俊杰", "filename", "Aways online.mp3")
    );

    @GetMapping("/list")
    public Result<List<Map<String, String>>> list() {
        List<Map<String, String>> musicList = builtinMusicList.stream()
            .map(m -> Map.of("id", m.get("id"), "name", m.get("name"), "artist", m.get("artist")))
            .toList();
        return Result.success(musicList);
    }

    @GetMapping("/play/{id}")
    public ResponseEntity<byte[]> play(@PathVariable String id) {
        try {
            var music = builtinMusicList.stream()
                .filter(m -> m.get("id").equals(id))
                .findFirst();
            
            if (music.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            
            String filename = music.get().get("filename");
            
            // 先获取文件信息
            String contentsUrl = String.format(
                "https://gitee.com/api/v5/repos/%s/%s/contents/%s?ref=%s",
                GITEE_OWNER, GITEE_REPO,
                java.net.URLEncoder.encode(filename, java.nio.charset.StandardCharsets.UTF_8),
                GITEE_BRANCH
            );
            
            String contentsResp = HttpUtil.createGet(contentsUrl)
                .header("Accept", "application/json")
                .execute()
                .body();
            
            JSONObject json = JSONUtil.parseObj(contentsResp);
            String downloadUrl = json.getStr("download_url", "");
            
            if (downloadUrl.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            
            // 下载音频文件
            byte[] audioData = HttpUtil.createGet(downloadUrl)
                .header("Accept", "audio/mpeg")
                .execute()
                .bodyBytes();
            
            if (audioData == null || audioData.length == 0) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            
            return ResponseEntity.ok()
                .header("Content-Type", "audio/mpeg")
                .header("Accept-Ranges", "bytes")
                .body(audioData);
        } catch (Exception e) {
            System.err.println("音乐播放失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
