package com.yaoyao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yaoyao.entity.Diary;
import com.yaoyao.mapper.DiaryMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DiaryService 日记服务测试")
class DiaryServiceImplTest {

    @Mock
    private DiaryMapper diaryMapper;

    @InjectMocks
    private DiaryServiceImpl diaryService;

    @Nested
    @DisplayName("create 创建日记测试")
    class CreateTests {

        @Test
        @DisplayName("正常创建日记")
        void createDiarySuccessfully() {
            when(diaryMapper.insert(any(Diary.class))).thenReturn(1);

            Diary diary = new Diary();
            diary.setTitle("今天");
            diary.setContent("开心的一天");
            diary.setDate(LocalDate.now());

            Diary result = diaryService.create(diary);

            assertNotNull(result);
            assertEquals("今天", result.getTitle());
            assertEquals("开心的一天", result.getContent());
            assertEquals("happy", result.getMood());
            assertNotNull(result.getCreatedAt());
        }

        @Test
        @DisplayName("标题为空抛出异常")
        void createWithBlankTitle() {
            Diary diary = new Diary();
            diary.setTitle("");
            diary.setContent("内容");

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> diaryService.create(diary));
            assertEquals("标题和内容不能为空", ex.getMessage());
        }

        @Test
        @DisplayName("内容为空抛出异常")
        void createWithBlankContent() {
            Diary diary = new Diary();
            diary.setTitle("标题");
            diary.setContent("");

            assertThrows(IllegalArgumentException.class,
                    () -> diaryService.create(diary));
        }

        @Test
        @DisplayName("mood为null时默认happy")
        void createWithDefaultMood() {
            when(diaryMapper.insert(any(Diary.class))).thenReturn(1);

            Diary diary = new Diary();
            diary.setTitle("标题");
            diary.setContent("内容");
            diary.setMood(null);

            diaryService.create(diary);
            assertEquals("happy", diary.getMood());
        }

        @Test
        @DisplayName("date为null时默认今天")
        void createWithDefaultDate() {
            when(diaryMapper.insert(any(Diary.class))).thenReturn(1);

            Diary diary = new Diary();
            diary.setTitle("标题");
            diary.setContent("内容");
            diary.setDate(null);

            diaryService.create(diary);
            assertEquals(LocalDate.now(), diary.getDate());
        }

        @Test
        @DisplayName("标题和内容自动trim")
        void createTrimsTitleAndContent() {
            when(diaryMapper.insert(any(Diary.class))).thenReturn(1);

            Diary diary = new Diary();
            diary.setTitle("  标题  ");
            diary.setContent("  内容  ");

            diaryService.create(diary);
            assertEquals("标题", diary.getTitle());
            assertEquals("内容", diary.getContent());
        }
    }

    @Nested
    @DisplayName("update 更新日记测试")
    class UpdateTests {

        @Test
        @DisplayName("正常更新日记")
        void updateDiarySuccessfully() {
            Diary existing = new Diary();
            existing.setId("1");
            existing.setTitle("旧标题");

            when(diaryMapper.update(any(), any())).thenReturn(1);
            when(diaryMapper.selectById("1")).thenReturn(existing);

            Diary updateData = new Diary();
            updateData.setTitle("新标题");

            Diary result = diaryService.update("1", updateData);
            assertNotNull(result);
            verify(diaryMapper).update(any(), any());
        }

        @Test
        @DisplayName("部分字段更新-仅更新标题")
        void updateOnlyTitle() {
            when(diaryMapper.update(any(), any())).thenReturn(1);
            when(diaryMapper.selectById("1")).thenReturn(new Diary());

            Diary updateData = new Diary();
            updateData.setTitle("新标题");
            updateData.setContent(null);
            updateData.setMood(null);
            updateData.setDate(null);

            diaryService.update("1", updateData);
            verify(diaryMapper).update(any(), any());
        }
    }

    @Nested
    @DisplayName("delete 删除日记测试")
    class DeleteTests {

        @Test
        @DisplayName("正常删除日记")
        void deleteDiarySuccessfully() {
            when(diaryMapper.deleteById("1")).thenReturn(1);
            diaryService.delete("1");
            verify(diaryMapper).deleteById("1");
        }
    }

    @Nested
    @DisplayName("list 查询日记测试")
    class ListTests {

        @Test
        @DisplayName("查询所有日记")
        void listAllDiaries() {
            Diary d1 = new Diary();
            d1.setId("1");
            Diary d2 = new Diary();
            d2.setId("2");

            when(diaryMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Arrays.asList(d1, d2));

            var result = diaryService.list();
            assertEquals(2, result.size());
        }

        @Test
        @DisplayName("无日记返回空列表")
        void listEmptyDiaries() {
            when(diaryMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(Collections.emptyList());

            var result = diaryService.list();
            assertTrue(result.isEmpty());
        }
    }
}
