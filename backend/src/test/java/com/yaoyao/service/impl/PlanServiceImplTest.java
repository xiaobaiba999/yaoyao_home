package com.yaoyao.service.impl;

import com.yaoyao.entity.Plan;
import com.yaoyao.mapper.PlanMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlanService 计划服务测试")
class PlanServiceImplTest {

    @Mock
    private PlanMapper planMapper;

    @InjectMocks
    private PlanServiceImpl planService;

    @Nested
    @DisplayName("create 创建计划测试")
    class CreateTests {

        @Test
        @DisplayName("正常创建计划")
        void createPlanSuccessfully() {
            when(planMapper.insert(any(Plan.class))).thenReturn(1);

            Plan plan = new Plan();
            plan.setTitle("一起去海边");
            plan.setTargetDate(LocalDate.of(2025, 8, 1));

            Plan result = planService.create(plan);

            assertNotNull(result);
            assertEquals("一起去海边", result.getTitle());
            assertEquals("pending", result.getStatus());
            assertNotNull(result.getCreatedAt());
        }

        @Test
        @DisplayName("标题为空抛出异常")
        void createWithBlankTitle() {
            Plan plan = new Plan();
            plan.setTitle("");

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> planService.create(plan));
            assertEquals("计划标题不能为空", ex.getMessage());
        }

        @Test
        @DisplayName("描述为空时设为空字符串")
        void createWithBlankDescription() {
            when(planMapper.insert(any(Plan.class))).thenReturn(1);

            Plan plan = new Plan();
            plan.setTitle("标题");
            plan.setDescription(null);

            planService.create(plan);
            assertEquals("", plan.getDescription());
        }

        @Test
        @DisplayName("新建计划默认状态pending")
        void createDefaultStatus() {
            when(planMapper.insert(any(Plan.class))).thenReturn(1);

            Plan plan = new Plan();
            plan.setTitle("标题");

            planService.create(plan);
            assertEquals("pending", plan.getStatus());
        }

        @Test
        @DisplayName("标题自动trim")
        void createTrimsTitle() {
            when(planMapper.insert(any(Plan.class))).thenReturn(1);

            Plan plan = new Plan();
            plan.setTitle("  标题  ");

            planService.create(plan);
            assertEquals("标题", plan.getTitle());
        }
    }

    @Nested
    @DisplayName("update 更新计划测试")
    class UpdateTests {

        @Test
        @DisplayName("更新计划状态")
        void updatePlanStatus() {
            when(planMapper.update(any(), any())).thenReturn(1);
            when(planMapper.selectById("1")).thenReturn(new Plan());

            Plan updateData = new Plan();
            updateData.setStatus("completed");

            planService.update("1", updateData);
            verify(planMapper).update(any(), any());
        }

        @Test
        @DisplayName("更新计划标题和目标日期")
        void updatePlanTitleAndDate() {
            when(planMapper.update(any(), any())).thenReturn(1);
            when(planMapper.selectById("1")).thenReturn(new Plan());

            Plan updateData = new Plan();
            updateData.setTitle("新标题");
            updateData.setTargetDate(LocalDate.of(2025, 12, 31));

            planService.update("1", updateData);
            verify(planMapper).update(any(), any());
        }
    }

    @Nested
    @DisplayName("delete 删除计划测试")
    class DeleteTests {

        @Test
        @DisplayName("正常删除计划")
        void deletePlanSuccessfully() {
            when(planMapper.deleteById("1")).thenReturn(1);
            planService.delete("1");
            verify(planMapper).deleteById("1");
        }
    }
}
