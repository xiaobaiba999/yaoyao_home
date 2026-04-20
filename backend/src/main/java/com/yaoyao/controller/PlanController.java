package com.yaoyao.controller;

import com.yaoyao.common.Result;
import com.yaoyao.entity.Plan;
import com.yaoyao.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @GetMapping
    public Result<List<Plan>> list() {
        List<Plan> data = planService.list();
        return Result.success(data);
    }

    @PostMapping
    public Result<Plan> create(@RequestBody Plan plan) {
        Plan data = planService.create(plan);
        return Result.success(data);
    }

    @PutMapping("/{id}")
    public Result<Plan> update(@PathVariable String id, @RequestBody Plan plan) {
        Plan data = planService.update(id, plan);
        return Result.success(data);
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable String id) {
        planService.delete(id);
        return Result.success("删除成功");
    }
}
