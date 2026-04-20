package com.yaoyao.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yaoyao.common.PageResult;
import com.yaoyao.entity.Message;
import com.yaoyao.mapper.MessageMapper;
import com.yaoyao.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;

    @Override
    public PageResult<Message> list(int page, int limit) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Message::getCreatedAt);

        Page<Message> pageResult = messageMapper.selectPage(new Page<>(page, limit), wrapper);
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal(), page, limit);
    }

    @Override
    public Message create(Message message) {
        if (StrUtil.isBlank(message.getContent())) {
            throw new IllegalArgumentException("留言内容不能为空");
        }
        message.setContent(message.getContent().trim());
        if (message.getContent().length() > 500) {
            throw new IllegalArgumentException("留言内容不能超过500字");
        }
        message.setAuthor(StrUtil.isBlank(message.getAuthor()) ? "匿名" : message.getAuthor().trim());
        if (message.getAuthor().length() > 50) {
            message.setAuthor(message.getAuthor().substring(0, 50));
        }
        messageMapper.insert(message);
        return message;
    }

    @Override
    public void delete(String id) {
        messageMapper.deleteById(id);
    }
}
