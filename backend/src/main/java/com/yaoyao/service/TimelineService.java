package com.yaoyao.service;

import com.yaoyao.entity.Timeline;

import java.util.List;

public interface TimelineService {

    List<Timeline> list();

    Timeline create(Timeline timeline);

    Timeline update(String id, Timeline timeline);

    void delete(String id);
}
