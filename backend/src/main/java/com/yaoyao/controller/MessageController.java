package com.yaoyao.controller;

import com.yaoyao.common.PageResult;
import com.yaoyao.common.Result;
import com.yaoyao.entity.Message;
import com.yaoyao.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    public Result<PageResult<Message>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        PageResult<Message> data = messageService.list(page, limit);
        return Result.success(data);
    }

    @PostMapping
    public Result<Message> create(@RequestBody Message message) {
        Message data = messageService.create(message);
        return Result.success(data);
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable String id) {
        messageService.delete(id);
        return Result.success("删除成功");
    }
}
