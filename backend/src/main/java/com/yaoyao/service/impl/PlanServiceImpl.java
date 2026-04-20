package com.yaoyao.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.yaoyao.entity.Plan;
import com.yaoyao.mapper.PlanMapper;
import com.yaoyao.service.PlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanServiceImpl implements PlanService {

    private final PlanMapper planMapper;

    @Override
    public List<Plan> list() {
        LambdaQueryWrapper<Plan> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Plan::getTargetDate);
        return planMapper.selectList(wrapper);
    }

    @Override
    public Plan create(Plan plan) {
        if (StrUtil.isBlank(plan.getTitle())) {
            throw new IllegalArgumentException("计划标题不能为空");
        }
        plan.setTitle(plan.getTitle().trim());
        if (plan.getTitle().length() > 100) {
            throw new IllegalArgumentException("计划标题不能超过100字");
        }
        plan.setDescription(StrUtil.isBlank(plan.getDescription()) ? "" : plan.getDescription().trim());
        plan.setStatus("pending");
        plan.setCreatedAt(LocalDateTime.now());
        planMapper.insert(plan);
        return plan;
    }

    @Override
    public Plan update(String id, Plan plan) {
        LambdaUpdateWrapper<Plan> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Plan::getId, id);
        if (StrUtil.isNotBlank(plan.getTitle())) {
            wrapper.set(Plan::getTitle, plan.getTitle().trim());
        }
        if (plan.getDescription() != null) {
            wrapper.set(Plan::getDescription, StrUtil.isBlank(plan.getDescription()) ? "" : plan.getDescription().trim());
        }
        if (plan.getTargetDate() != null) {
            wrapper.set(Plan::getTargetDate, plan.getTargetDate());
        }
        if (StrUtil.isNotBlank(plan.getStatus())) {
            wrapper.set(Plan::getStatus, plan.getStatus());
        }
        wrapper.set(Plan::getUpdatedAt, LocalDateTime.now());
        planMapper.update(null, wrapper);
        return planMapper.selectById(id);
    }

    @Override
    public void delete(String id) {
        planMapper.deleteById(id);
    }
}
