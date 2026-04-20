package com.yaoyao.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaoyao.common.PageResult;
import com.yaoyao.entity.Message;
import com.yaoyao.service.MessageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("MessageController 留言接口测试")
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MessageService messageService;

    @Nested
    @DisplayName("GET /api/messages 获取留言列表测试")
    class ListTests {

        @Test
        @DisplayName("获取留言列表成功")
        void listMessagesSuccessfully() throws Exception {
            Message msg1 = new Message();
            msg1.setId("1");
            msg1.setContent("留言1");
            msg1.setAuthor("用户1");

            PageResult<Message> pageResult = new PageResult<>(Arrays.asList(msg1), 1, 1, 10);

            when(messageService.list(1, 10)).thenReturn(pageResult);

            mockMvc.perform(get("/api/messages")
                            .param("page", "1")
                            .param("limit", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.data[0].content").value("留言1"))
                    .andExpect(jsonPath("$.data.total").value(1));
        }

        @Test
        @DisplayName("使用默认分页参数")
        void listWithDefaultParams() throws Exception {
            PageResult<Message> pageResult = new PageResult<>(Arrays.asList(), 0, 1, 10);
            when(messageService.list(1, 10)).thenReturn(pageResult);

            mockMvc.perform(get("/api/messages"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    @Nested
    @DisplayName("POST /api/messages 创建留言测试")
    class CreateTests {

        @Test
        @DisplayName("创建留言成功")
        void createMessageSuccessfully() throws Exception {
            Message created = new Message();
            created.setId("1");
            created.setContent("新留言");
            created.setAuthor("用户");

            when(messageService.create(any(Message.class))).thenReturn(created);

            mockMvc.perform(post("/api/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    java.util.Map.of("content", "新留言", "author", "用户"))))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.content").value("新留言"));
        }

        @Test
        @DisplayName("内容为空创建留言返回400")
        void createWithEmptyContent() throws Exception {
            when(messageService.create(any(Message.class)))
                    .thenThrow(new IllegalArgumentException("留言内容不能为空"));

            mockMvc.perform(post("/api/messages")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    java.util.Map.of("content", "", "author", "用户"))))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(400));
        }
    }

    @Nested
    @DisplayName("DELETE /api/messages/{id} 删除留言测试")
    class DeleteTests {

        @Test
        @DisplayName("删除留言需要认证")
        void deleteMessageRequiresAuth() throws Exception {
            mockMvc.perform(delete("/api/messages/1"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
