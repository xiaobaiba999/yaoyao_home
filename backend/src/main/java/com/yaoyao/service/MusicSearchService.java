package com.yaoyao.service;

import java.util.List;

public interface MusicSearchService {
    List<MusicInfo> search(String keyword);
    String getPlayUrl(String songId);
}
