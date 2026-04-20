package com.yaoyao.controller;

import com.yaoyao.common.Result;
import com.yaoyao.entity.Timeline;
import com.yaoyao.service.TimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/timeline")
@RequiredArgsConstructor
public class TimelineController {

    private final TimelineService timelineService;

    @GetMapping
    public Result<List<Timeline>> list() {
        List<Timeline> data = timelineService.list();
        return Result.success(data);
    }

    @PostMapping
    public Result<Timeline> create(@RequestBody Timeline timeline) {
        Timeline data = timelineService.create(timeline);
        return Result.success(data);
    }

    @PutMapping("/{id}")
    public Result<Timeline> update(@PathVariable String id, @RequestBody Timeline timeline) {
        Timeline data = timelineService.update(id, timeline);
        return Result.success(data);
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable String id) {
        timelineService.delete(id);
        return Result.success("删除成功");
    }
}
