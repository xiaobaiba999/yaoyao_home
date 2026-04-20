package com.yaoyao.service;

import com.yaoyao.common.PageResult;
import com.yaoyao.entity.Message;

public interface MessageService {

    PageResult<Message> list(int page, int limit);

    Message create(Message message);

    void delete(String id);
}
