package com.yaoyao.service.impl;

import com.yaoyao.entity.Timeline;
import com.yaoyao.mapper.TimelineMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TimelineService 时间线服务测试")
class TimelineServiceImplTest {

    @Mock
    private TimelineMapper timelineMapper;

    @InjectMocks
    private TimelineServiceImpl timelineService;

    @Nested
    @DisplayName("create 创建时间线测试")
    class CreateTests {

        @Test
        @DisplayName("正常创建时间线")
        void createTimelineSuccessfully() {
            when(timelineMapper.insert(any(Timeline.class))).thenReturn(1);

            Timeline timeline = new Timeline();
            timeline.setDate("2024-01-01");
            timeline.setTitle("第一次见面");
            timeline.setDescription("在咖啡店相遇");

            Timeline result = timelineService.create(timeline);

            assertNotNull(result);
            assertEquals("2024-01-01", result.getDate());
            assertEquals("第一次见面", result.getTitle());
            assertNotNull(result.getCreatedAt());
        }

        @Test
        @DisplayName("日期为空抛出异常")
        void createWithBlankDate() {
            Timeline timeline = new Timeline();
            timeline.setDate("");
            timeline.setTitle("标题");
            timeline.setDescription("描述");

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> timelineService.create(timeline));
            assertEquals("请填写完整信息", ex.getMessage());
        }

        @Test
        @DisplayName("标题为空抛出异常")
        void createWithBlankTitle() {
            Timeline timeline = new Timeline();
            timeline.setDate("2024-01-01");
            timeline.setTitle("");
            timeline.setDescription("描述");

            assertThrows(IllegalArgumentException.class,
                    () -> timelineService.create(timeline));
        }

        @Test
        @DisplayName("描述为空抛出异常")
        void createWithBlankDescription() {
            Timeline timeline = new Timeline();
            timeline.setDate("2024-01-01");
            timeline.setTitle("标题");
            timeline.setDescription("");

            assertThrows(IllegalArgumentException.class,
                    () -> timelineService.create(timeline));
        }

        @Test
        @DisplayName("所有字段为null抛出异常")
        void createWithAllNullFields() {
            Timeline timeline = new Timeline();
            assertThrows(IllegalArgumentException.class,
                    () -> timelineService.create(timeline));
        }
    }

    @Nested
    @DisplayName("update 更新时间线测试")
    class UpdateTests {

        @Test
        @DisplayName("正常更新时间线")
        void updateTimelineSuccessfully() {
            when(timelineMapper.update(any(), any())).thenReturn(1);
            when(timelineMapper.selectById("1")).thenReturn(new Timeline());

            Timeline updateData = new Timeline();
            updateData.setDate("2024-06-01");
            updateData.setTitle("更新标题");
            updateData.setDescription("更新描述");

            timelineService.update("1", updateData);
            verify(timelineMapper).update(any(), any());
        }

        @Test
        @DisplayName("更新时字段不完整抛出异常")
        void updateWithIncompleteFields() {
            Timeline updateData = new Timeline();
            updateData.setDate("2024-06-01");
            updateData.setTitle("");
            updateData.setDescription("描述");

            assertThrows(IllegalArgumentException.class,
                    () -> timelineService.update("1", updateData));
        }
    }

    @Nested
    @DisplayName("delete 删除时间线测试")
    class DeleteTests {

        @Test
        @DisplayName("正常删除时间线")
        void deleteTimelineSuccessfully() {
            when(timelineMapper.deleteById("1")).thenReturn(1);
            timelineService.delete("1");
            verify(timelineMapper).deleteById("1");
        }
    }
}
