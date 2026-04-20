package com.yaoyao.service.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yaoyao.service.MusicInfo;
import com.yaoyao.service.MusicSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GequhaiMusicServiceImpl implements MusicSearchService {

    private static final Logger log = LoggerFactory.getLogger(GequhaiMusicServiceImpl.class);
    private static final String NETEASE_SEARCH_URL = "https://api.injahow.cn/search";
    private static final String NETEASE_SONG_URL = "https://api.injahow.cn/song/url";

    private final Map<String, String> songUrlCache = new HashMap<>();

    @Override
    public List<MusicInfo> search(String keyword) {
        List<MusicInfo> results = new ArrayList<>();
        try {
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String url = NETEASE_SEARCH_URL + "?keywords=" + encodedKeyword + "&limit=10";

            log.info("搜索音乐: {}", keyword);
            String response = HttpUtil.get(url, 10000);

            if (response == null || response.isEmpty()) {
                log.warn("搜索返回空结果");
                return results;
            }

            JSONObject json = JSONUtil.parseObj(response);
            
            int code = json.getInt("code", -1);
            if (code != 200) {
                log.warn("搜索API返回错误码: {}", code);
                return results;
            }

            JSONObject result = json.getJSONObject("result");
            if (result == null) {
                log.info("搜索结果为空");
                return results;
            }

            var songs = result.getJSONArray("songs");
            if (songs == null || songs.isEmpty()) {
                log.info("未找到歌曲");
                return results;
            }

            for (int i = 0; i < Math.min(songs.size(), 10); i++) {
                JSONObject song = songs.getJSONObject(i);
                MusicInfo info = new MusicInfo();
                
                info.setId(song.getStr("id", String.valueOf(i)));
                info.setName(song.getStr("name", "未知歌曲"));
                
                var artists = song.getJSONArray("artists");
                if (artists != null && !artists.isEmpty()) {
                    info.setArtist(artists.getJSONObject(0).getStr("name", "未知歌手"));
                } else {
                    info.setArtist("未知歌手");
                }
                
                JSONObject album = song.getJSONObject("album");
                if (album != null) {
                    info.setAlbum(album.getStr("name", ""));
                    info.setCover(album.getStr("picUrl", ""));
                }
                
                Long duration = song.getLong("duration", 0L);
                if (duration > 0) {
                    long minutes = duration / 60000;
                    long seconds = (duration % 60000) / 1000;
                    info.setDuration(String.format("%02d:%02d", minutes, seconds));
                }
                
                String songId = info.getId();
                if (songUrlCache.containsKey(songId)) {
                    info.setUrl(songUrlCache.get(songId));
                }
                
                results.add(info);
            }
            
            log.info("搜索完成: 找到 {} 首歌曲", results.size());
        } catch (Exception e) {
            log.error("搜索音乐异常: {}", e.getMessage(), e);
        }

        return results;
    }

    @Override
    public String getPlayUrl(String songId) {
        try {
            if (songUrlCache.containsKey(songId)) {
                return songUrlCache.get(songId);
            }

            String url = NETEASE_SONG_URL + "?id=" + songId;
            log.info("获取歌曲URL: {}", songId);
            
            String response = HttpUtil.get(url, 10000);
            
            if (response == null || response.isEmpty()) {
                return "";
            }

            JSONObject json = JSONUtil.parseObj(response);
            
            if (json.getInt("code", -1) == 200) {
                var songs = json.getJSONArray("songs");
                if (songs != null && !songs.isEmpty()) {
                    JSONObject song = songs.getJSONObject(0);
                    String playUrl = song.getStr("url", "");
                    
                    if (playUrl != null && !playUrl.isEmpty()) {
                        songUrlCache.put(songId, playUrl);
                        log.info("获取歌曲URL成功: {}", playUrl);
                        return playUrl;
                    }
                }
            }
            
            log.warn("获取歌曲URL失败: {}", songId);
        } catch (Exception e) {
            log.error("获取播放URL异常: {}", e.getMessage(), e);
        }
        
        return "";
    }
}
