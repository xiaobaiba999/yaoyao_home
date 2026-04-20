package com.yaoyao.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yaoyao.entity.Diary;
import com.yaoyao.mapper.DiaryMapper;
import com.yaoyao.service.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryServiceImpl implements DiaryService {

    private final DiaryMapper diaryMapper;

    @Override
    public List<Diary> list() {
        LambdaQueryWrapper<Diary> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Diary::getDate);
        return diaryMapper.selectList(wrapper);
    }

    @Override
    public Diary create(Diary diary) {
        if (StrUtil.isBlank(diary.getTitle()) || StrUtil.isBlank(diary.getContent())) {
            throw new IllegalArgumentException("标题和内容不能为空");
        }
        diary.setTitle(diary.getTitle().trim());
        if (diary.getTitle().length() > 100) {
            throw new IllegalArgumentException("标题不能超过100字");
        }
        diary.setContent(diary.getContent().trim());
        if (diary.getContent().length() > 10000) {
            throw new IllegalArgumentException("内容不能超过10000字");
        }
        if (diary.getMood() == null) {
            diary.setMood("happy");
        }
        if (diary.getDate() == null) {
            diary.setDate(LocalDate.now());
        }
        diary.setCreatedAt(LocalDateTime.now());
        diaryMapper.insert(diary);
        return diary;
    }

    @Override
    public Diary update(String id, Diary diary) {
        LambdaUpdateWrapper<Diary> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Diary::getId, id);
        if (diary.getTitle() != null) {
            wrapper.set(Diary::getTitle, diary.getTitle().trim());
        }
        if (diary.getContent() != null) {
            wrapper.set(Diary::getContent, diary.getContent().trim());
        }
        if (diary.getMood() != null) {
            wrapper.set(Diary::getMood, diary.getMood());
        }
        if (diary.getDate() != null) {
            wrapper.set(Diary::getDate, diary.getDate());
        }
        wrapper.set(Diary::getUpdatedAt, LocalDateTime.now());
        diaryMapper.update(null, wrapper);
        return diaryMapper.selectById(id);
    }

    @Override
    public void delete(String id) {
        diaryMapper.deleteById(id);
    }
}
