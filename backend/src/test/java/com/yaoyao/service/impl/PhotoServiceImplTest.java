package com.yaoyao.service.impl;

import com.yaoyao.entity.Photo;
import com.yaoyao.mapper.PhotoMapper;
import com.yaoyao.service.GiteeStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PhotoService 照片服务测试")
class PhotoServiceImplTest {

    @Mock
    private PhotoMapper photoMapper;

    @Mock
    private GiteeStorageService giteeStorageService;

    @InjectMocks
    private PhotoServiceImpl photoService;

    @Nested
    @DisplayName("upload 上传照片测试")
    class UploadTests {

        @Test
        @DisplayName("正常上传图片")
        void uploadPhotoSuccessfully() {
            MockMultipartFile file = new MockMultipartFile(
                    "photo", "test.jpg", "image/jpeg", "test image content".getBytes());

            when(giteeStorageService.uploadFile(any(MultipartFile.class)))
                    .thenReturn("https://gitee.com/user/repo/raw/images/photo-test.jpg");
            when(photoMapper.insert(any(Photo.class))).thenReturn(1);

            Photo result = photoService.upload(file, "测试描述");

            assertNotNull(result);
            assertEquals("test.jpg", result.getFilename());
            assertEquals("https://gitee.com/user/repo/raw/images/photo-test.jpg", result.getUrl());
            assertEquals("测试描述", result.getDescription());
            assertNotNull(result.getCreatedAt());
        }

        @Test
        @DisplayName("文件为null抛出异常")
        void uploadWithNullFile() {
            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> photoService.upload(null, ""));
            assertEquals("请选择要上传的图片", ex.getMessage());
        }

        @Test
        @DisplayName("空文件抛出异常")
        void uploadWithEmptyFile() {
            MockMultipartFile emptyFile = new MockMultipartFile(
                    "photo", "empty.jpg", "image/jpeg", new byte[0]);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> photoService.upload(emptyFile, ""));
            assertEquals("请选择要上传的图片", ex.getMessage());
        }

        @Test
        @DisplayName("非图片文件抛出异常")
        void uploadNonImageFile() {
            MockMultipartFile textFile = new MockMultipartFile(
                    "photo", "test.txt", "text/plain", "text content".getBytes());

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> photoService.upload(textFile, ""));
            assertEquals("只支持图片文件 (jpeg, jpg, png, gif, webp)", ex.getMessage());
        }

        @Test
        @DisplayName("contentType为null抛出异常")
        void uploadWithNullContentType() {
            MockMultipartFile file = new MockMultipartFile(
                    "photo", "test.jpg", null, "content".getBytes());

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> photoService.upload(file, ""));
            assertEquals("只支持图片文件 (jpeg, jpg, png, gif, webp)", ex.getMessage());
        }

        @Test
        @DisplayName("描述超过20字符自动截断")
        void uploadTruncatesLongDescription() {
            MockMultipartFile file = new MockMultipartFile(
                    "photo", "test.jpg", "image/jpeg", "content".getBytes());

            when(giteeStorageService.uploadFile(any(MultipartFile.class)))
                    .thenReturn("https://gitee.com/test.jpg");
            when(photoMapper.insert(any(Photo.class))).thenReturn(1);

            String longDesc = "这是一段非常非常长的描述文字超过了二十个字符的限制";
            Photo result = photoService.upload(file, longDesc);

            assertTrue(result.getDescription().length() <= 20);
        }

        @Test
        @DisplayName("描述为空时设为空字符串")
        void uploadWithEmptyDescription() {
            MockMultipartFile file = new MockMultipartFile(
                    "photo", "test.jpg", "image/jpeg", "content".getBytes());

            when(giteeStorageService.uploadFile(any(MultipartFile.class)))
                    .thenReturn("https://gitee.com/test.jpg");
            when(photoMapper.insert(any(Photo.class))).thenReturn(1);

            Photo result = photoService.upload(file, "");
            assertEquals("", result.getDescription());
        }

        @Test
        @DisplayName("支持PNG格式上传")
        void uploadPngImage() {
            MockMultipartFile file = new MockMultipartFile(
                    "photo", "test.png", "image/png", "content".getBytes());

            when(giteeStorageService.uploadFile(any(MultipartFile.class)))
                    .thenReturn("https://gitee.com/test.png");
            when(photoMapper.insert(any(Photo.class))).thenReturn(1);

            Photo result = photoService.upload(file, "");
            assertNotNull(result);
        }

        @Test
        @DisplayName("支持WebP格式上传")
        void uploadWebpImage() {
            MockMultipartFile file = new MockMultipartFile(
                    "photo", "test.webp", "image/webp", "content".getBytes());

            when(giteeStorageService.uploadFile(any(MultipartFile.class)))
                    .thenReturn("https://gitee.com/test.webp");
            when(photoMapper.insert(any(Photo.class))).thenReturn(1);

            Photo result = photoService.upload(file, "");
            assertNotNull(result);
        }
    }

    @Nested
    @DisplayName("delete 删除照片测试")
    class DeleteTests {

        @Test
        @DisplayName("删除Gitee图片时同步删除远程文件")
        void deleteGiteePhotoAlsoDeletesRemote() {
            Photo photo = new Photo();
            photo.setId("1");
            photo.setFilename("photo-test.jpg");
            photo.setUrl("https://gitee.com/user/repo/raw/images/photo-test.jpg");

            when(photoMapper.selectById("1")).thenReturn(photo);
            when(photoMapper.deleteById("1")).thenReturn(1);

            photoService.delete("1");

            verify(giteeStorageService).deleteFile("photo-test.jpg");
            verify(photoMapper).deleteById("1");
        }

        @Test
        @DisplayName("删除非Gitee图片时不删除远程文件")
        void deleteNonGiteePhotoSkipsRemoteDelete() {
            Photo photo = new Photo();
            photo.setId("2");
            photo.setFilename("old-photo.jpg");
            photo.setUrl("https://supabase.co/storage/v1/object/photos/old-photo.jpg");

            when(photoMapper.selectById("2")).thenReturn(photo);
            when(photoMapper.deleteById("2")).thenReturn(1);

            photoService.delete("2");

            verify(giteeStorageService, never()).deleteFile(anyString());
            verify(photoMapper).deleteById("2");
        }

        @Test
        @DisplayName("删除不存在的照片不报错")
        void deleteNonExistentPhoto() {
            when(photoMapper.selectById("999")).thenReturn(null);
            when(photoMapper.deleteById("999")).thenReturn(0);

            photoService.delete("999");

            verify(giteeStorageService, never()).deleteFile(anyString());
        }

        @Test
        @DisplayName("Gitee远程删除失败时不影响数据库删除")
        void deletePhotoWhenGiteeDeleteFails() {
            Photo photo = new Photo();
            photo.setId("1");
            photo.setFilename("photo-test.jpg");
            photo.setUrl("https://gitee.com/user/repo/raw/images/photo-test.jpg");

            when(photoMapper.selectById("1")).thenReturn(photo);
            doThrow(new RuntimeException("Gitee删除失败")).when(giteeStorageService).deleteFile(anyString());
            when(photoMapper.deleteById("1")).thenReturn(1);

            assertDoesNotThrow(() -> photoService.delete("1"));
            verify(photoMapper).deleteById("1");
        }
    }
}
