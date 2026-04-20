package com.yaoyao.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yaoyao.entity.Wish;
import com.yaoyao.mapper.WishMapper;
import com.yaoyao.service.WishService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WishServiceImpl implements WishService {

    private final WishMapper wishMapper;

    @Override
    public List<Wish> list() {
        LambdaQueryWrapper<Wish> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Wish::getCreatedAt);
        return wishMapper.selectList(wrapper);
    }

    @Override
    public Wish create(String content) {
        if (StrUtil.isBlank(content)) {
            throw new IllegalArgumentException("心愿内容不能为空");
        }
        if (content.trim().length() > 200) {
            throw new IllegalArgumentException("心愿内容不能超过200字");
        }
        Wish wish = new Wish();
        wish.setContent(content.trim());
        wish.setCompleted(false);
        wish.setCreatedAt(LocalDateTime.now());
        wishMapper.insert(wish);
        return wish;
    }

    @Override
    public Wish update(String id, Boolean completed, String content) {
        LambdaUpdateWrapper<Wish> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Wish::getId, id);
        if (completed != null) {
            wrapper.set(Wish::getCompleted, completed);
        }
        if (StrUtil.isNotBlank(content)) {
            wrapper.set(Wish::getContent, content.trim());
        }
        wrapper.set(Wish::getUpdatedAt, LocalDateTime.now());
        wishMapper.update(null, wrapper);
        return wishMapper.selectById(id);
    }

    @Override
    public void delete(String id) {
        wishMapper.deleteById(id);
    }
}
