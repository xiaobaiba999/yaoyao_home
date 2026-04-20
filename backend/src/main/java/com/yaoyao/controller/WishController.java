package com.yaoyao.controller;

import com.yaoyao.common.Result;
import com.yaoyao.entity.Wish;
import com.yaoyao.service.WishService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wishes")
@RequiredArgsConstructor
public class WishController {

    private final WishService wishService;

    @GetMapping
    public Result<List<Wish>> list() {
        List<Wish> data = wishService.list();
        return Result.success(data);
    }

    @PostMapping
    public Result<Wish> create(@RequestBody Map<String, String> body) {
        Wish data = wishService.create(body.get("content"));
        return Result.success(data);
    }

    @PutMapping("/{id}")
    public Result<Wish> update(@PathVariable String id, @RequestBody Map<String, Object> body) {
        Boolean completed = body.get("completed") != null ? (Boolean) body.get("completed") : null;
        String content = body.get("content") != null ? body.get("content").toString() : null;
        Wish data = wishService.update(id, completed, content);
        return Result.success(data);
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable String id) {
        wishService.delete(id);
        return Result.success("删除成功");
    }
}
