package com.yaoyao.service;

import com.yaoyao.entity.Diary;

import java.util.List;

public interface DiaryService {

    List<Diary> list();

    Diary create(Diary diary);

    Diary update(String id, Diary diary);

    void delete(String id);
}
