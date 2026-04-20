package com.yaoyao.service.impl;

import com.yaoyao.entity.Wish;
import com.yaoyao.mapper.WishMapper;
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
@DisplayName("WishService 心愿服务测试")
class WishServiceImplTest {

    @Mock
    private WishMapper wishMapper;

    @InjectMocks
    private WishServiceImpl wishService;

    @Nested
    @DisplayName("create 创建心愿测试")
    class CreateTests {

        @Test
        @DisplayName("正常创建心愿")
        void createWishSuccessfully() {
            when(wishMapper.insert(any(Wish.class))).thenReturn(1);

            Wish result = wishService.create("一起去旅行");

            assertNotNull(result);
            assertEquals("一起去旅行", result.getContent());
            assertFalse(result.getCompleted());
            assertNotNull(result.getCreatedAt());
        }

        @Test
        @DisplayName("内容为空抛出异常")
        void createWithBlankContent() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> wishService.create(""));
            assertEquals("心愿内容不能为空", ex.getMessage());
        }

        @Test
        @DisplayName("内容为null抛出异常")
        void createWithNullContent() {
            assertThrows(IllegalArgumentException.class,
                    () -> wishService.create(null));
        }

        @Test
        @DisplayName("内容自动trim")
        void createTrimsContent() {
            when(wishMapper.insert(any(Wish.class))).thenReturn(1);

            Wish result = wishService.create("  一起去旅行  ");
            assertEquals("一起去旅行", result.getContent());
        }

        @Test
        @DisplayName("新建心愿默认未完成")
        void createDefaultNotCompleted() {
            when(wishMapper.insert(any(Wish.class))).thenReturn(1);

            Wish result = wishService.create("测试心愿");
            assertFalse(result.getCompleted());
        }
    }

    @Nested
    @DisplayName("update 更新心愿测试")
    class UpdateTests {

        @Test
        @DisplayName("标记心愿为已完成")
        void markWishAsCompleted() {
            Wish existing = new Wish();
            existing.setId("1");
            existing.setCompleted(false);

            when(wishMapper.update(any(), any())).thenReturn(1);
            when(wishMapper.selectById("1")).thenReturn(existing);

            wishService.update("1", true, null);
            verify(wishMapper).update(any(), any());
        }

        @Test
        @DisplayName("更新心愿内容")
        void updateWishContent() {
            Wish existing = new Wish();
            existing.setId("1");

            when(wishMapper.update(any(), any())).thenReturn(1);
            when(wishMapper.selectById("1")).thenReturn(existing);

            wishService.update("1", null, "新内容");
            verify(wishMapper).update(any(), any());
        }

        @Test
        @DisplayName("completed和content都为null时不更新对应字段")
        void updateWithNullFields() {
            when(wishMapper.update(any(), any())).thenReturn(1);
            when(wishMapper.selectById("1")).thenReturn(new Wish());

            wishService.update("1", null, null);
            verify(wishMapper).update(any(), any());
        }
    }

    @Nested
    @DisplayName("delete 删除心愿测试")
    class DeleteTests {

        @Test
        @DisplayName("正常删除心愿")
        void deleteWishSuccessfully() {
            when(wishMapper.deleteById("1")).thenReturn(1);
            wishService.delete("1");
            verify(wishMapper).deleteById("1");
        }
    }
}
