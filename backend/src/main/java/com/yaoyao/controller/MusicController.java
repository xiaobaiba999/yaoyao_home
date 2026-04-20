package com.yaoyao.controller;

import com.yaoyao.service.MusicInfo;
import com.yaoyao.service.MusicSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/music")
@RequiredArgsConstructor
public class MusicController {

    private final MusicSearchService musicSearchService;

    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam String keyword) {
        Map<String, Object> result = new HashMap<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            result.put("code", 400);
            result.put("message", "搜索关键词不能为空");
            return result;
        }
        List<MusicInfo> musicList = musicSearchService.search(keyword.trim());
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", musicList);
        return result;
    }

    @GetMapping("/url")
    public Map<String, Object> getPlayUrl(@RequestParam String id) {
        Map<String, Object> result = new HashMap<>();
        if (id == null || id.trim().isEmpty()) {
            result.put("code", 400);
            result.put("message", "歌曲ID不能为空");
            return result;
        }
        String url = musicSearchService.getPlayUrl(id.trim());
        if (url == null || url.isEmpty()) {
            result.put("code", 404);
            result.put("message", "未找到可播放的链接");
            return result;
        }
        result.put("code", 200);
        result.put("message", "success");
        result.put("data", url);
        return result;
    }
}
