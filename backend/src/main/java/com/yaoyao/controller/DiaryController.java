package com.yaoyao.controller;

import com.yaoyao.common.Result;
import com.yaoyao.entity.Diary;
import com.yaoyao.service.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    @GetMapping
    public Result<List<Diary>> list() {
        List<Diary> data = diaryService.list();
        return Result.success(data);
    }

    @PostMapping
    public Result<Diary> create(@RequestBody Diary diary) {
        Diary data = diaryService.create(diary);
        return Result.success(data);
    }

    @PutMapping("/{id}")
    public Result<Diary> update(@PathVariable String id, @RequestBody Diary diary) {
        Diary data = diaryService.update(id, diary);
        return Result.success(data);
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable String id) {
        diaryService.delete(id);
        return Result.success("删除成功");
    }
}
