package com.yaoyao.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yaoyao.entity.Timeline;
import com.yaoyao.mapper.TimelineMapper;
import com.yaoyao.service.TimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TimelineServiceImpl implements TimelineService {

    private final TimelineMapper timelineMapper;

    @Override
    public List<Timeline> list() {
        LambdaQueryWrapper<Timeline> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Timeline::getCreatedAt);
        return timelineMapper.selectList(wrapper);
    }

    @Override
    public Timeline create(Timeline timeline) {
        if (StrUtil.isBlank(timeline.getDate()) || StrUtil.isBlank(timeline.getTitle()) || StrUtil.isBlank(timeline.getDescription())) {
            throw new IllegalArgumentException("请填写完整信息");
        }
        if (timeline.getTitle().length() > 100) {
            throw new IllegalArgumentException("标题不能超过100字");
        }
        if (timeline.getDescription().length() > 500) {
            throw new IllegalArgumentException("描述不能超过500字");
        }
        timeline.setCreatedAt(LocalDateTime.now());
        timelineMapper.insert(timeline);
        return timeline;
    }

    @Override
    public Timeline update(String id, Timeline timeline) {
        if (StrUtil.isBlank(timeline.getDate()) || StrUtil.isBlank(timeline.getTitle()) || StrUtil.isBlank(timeline.getDescription())) {
            throw new IllegalArgumentException("请填写完整信息");
        }
        LambdaUpdateWrapper<Timeline> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Timeline::getId, id)
                .set(Timeline::getDate, timeline.getDate())
                .set(Timeline::getTitle, timeline.getTitle())
                .set(Timeline::getDescription, timeline.getDescription());
        timelineMapper.update(null, wrapper);
        return timelineMapper.selectById(id);
    }

    @Override
    public void delete(String id) {
        timelineMapper.deleteById(id);
    }
}
