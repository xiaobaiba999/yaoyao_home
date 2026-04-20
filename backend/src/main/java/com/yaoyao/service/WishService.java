package com.yaoyao.service;

import com.yaoyao.entity.Wish;

import java.util.List;

public interface WishService {

    List<Wish> list();

    Wish create(String content);

    Wish update(String id, Boolean completed, String content);

    void delete(String id);
}
