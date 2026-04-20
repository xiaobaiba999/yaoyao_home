package com.yaoyao.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yaoyao.common.PageResult;
import com.yaoyao.entity.Message;
import com.yaoyao.mapper.MessageMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageService 留言服务测试")
class MessageServiceImplTest {

    @Mock
    private MessageMapper messageMapper;

    @InjectMocks
    private MessageServiceImpl messageService;

    @Nested
    @DisplayName("list 分页查询测试")
    class ListTests {

        @Test
        @DisplayName("正常分页查询返回数据")
        void listWithMessages() {
            Message msg1 = new Message();
            msg1.setId("1");
            msg1.setContent("留言1");
            Message msg2 = new Message();
            msg2.setId("2");
            msg2.setContent("留言2");

            Page<Message> page = new Page<>(1, 10);
            page.setRecords(Arrays.asList(msg1, msg2));
            page.setTotal(2);

            when(messageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            PageResult<Message> result = messageService.list(1, 10);

            assertNotNull(result);
            assertEquals(2, result.getData().size());
            assertEquals(2, result.getTotal());
            assertEquals(1, result.getPage());
            assertFalse(result.isHasMore());
        }

        @Test
        @DisplayName("空数据分页查询")
        void listWithNoMessages() {
            Page<Message> page = new Page<>(1, 10);
            page.setRecords(Collections.emptyList());
            page.setTotal(0);

            when(messageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            PageResult<Message> result = messageService.list(1, 10);

            assertNotNull(result);
            assertTrue(result.getData().isEmpty());
            assertEquals(0, result.getTotal());
        }

        @Test
        @DisplayName("分页hasMore正确计算")
        void listHasMoreCalculation() {
            Page<Message> page = new Page<>(1, 10);
            page.setRecords(Collections.emptyList());
            page.setTotal(25);

            when(messageMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class))).thenReturn(page);

            PageResult<Message> result = messageService.list(1, 10);

            assertTrue(result.isHasMore());
        }
    }

    @Nested
    @DisplayName("create 创建留言测试")
    class CreateTests {

        @Test
        @DisplayName("正常创建留言")
        void createMessageSuccessfully() {
            when(messageMapper.insert(any(Message.class))).thenReturn(1);

            Message message = new Message();
            message.setContent("测试留言");
            message.setAuthor("测试用户");

            Message result = messageService.create(message);

            assertNotNull(result);
            assertEquals("测试留言", result.getContent());
            assertEquals("测试用户", result.getAuthor());
            verify(messageMapper).insert(any(Message.class));
        }

        @Test
        @DisplayName("内容为空抛出异常")
        void createWithBlankContent() {
            Message message = new Message();
            message.setContent("");

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> messageService.create(message));
            assertEquals("留言内容不能为空", ex.getMessage());
        }

        @Test
        @DisplayName("内容为null抛出异常")
        void createWithNullContent() {
            Message message = new Message();
            message.setContent(null);

            assertThrows(IllegalArgumentException.class,
                    () -> messageService.create(message));
        }

        @Test
        @DisplayName("内容前后空格自动trim")
        void createTrimsContent() {
            when(messageMapper.insert(any(Message.class))).thenReturn(1);

            Message message = new Message();
            message.setContent("  测试留言  ");

            messageService.create(message);
            assertEquals("测试留言", message.getContent());
        }

        @Test
        @DisplayName("作者为空时默认匿名")
        void createWithDefaultAuthor() {
            when(messageMapper.insert(any(Message.class))).thenReturn(1);

            Message message = new Message();
            message.setContent("测试留言");
            message.setAuthor(null);

            messageService.create(message);
            assertEquals("匿名", message.getAuthor());
        }

        @Test
        @DisplayName("作者前后空格自动trim")
        void createTrimsAuthor() {
            when(messageMapper.insert(any(Message.class))).thenReturn(1);

            Message message = new Message();
            message.setContent("测试留言");
            message.setAuthor("  用户  ");

            messageService.create(message);
            assertEquals("用户", message.getAuthor());
        }
    }

    @Nested
    @DisplayName("delete 删除留言测试")
    class DeleteTests {

        @Test
        @DisplayName("正常删除留言")
        void deleteMessageSuccessfully() {
            when(messageMapper.deleteById("1")).thenReturn(1);

            messageService.delete("1");

            verify(messageMapper).deleteById("1");
        }
    }
}
