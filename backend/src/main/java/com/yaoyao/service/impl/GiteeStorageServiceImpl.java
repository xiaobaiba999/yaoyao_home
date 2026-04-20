package com.yaoyao.service.impl;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yaoyao.config.AppProperties;
import com.yaoyao.service.GiteeStorageService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GiteeStorageServiceImpl implements GiteeStorageService {

    private static final Logger log = LoggerFactory.getLogger(GiteeStorageServiceImpl.class);
    private final AppProperties appProperties;

    @Override
    public String uploadFile(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (StrUtil.isNotBlank(originalFilename) && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = "photo-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8) + ext;
            String path = appProperties.getGitee().getPath() + "/" + filename;

            byte[] fileBytes = file.getBytes();
            String base64Content = Base64.getEncoder().encodeToString(fileBytes);

            String url = appProperties.getGitee().getBaseUrl()
                    + "/repos/" + appProperties.getGitee().getOwner()
                    + "/" + appProperties.getGitee().getRepo()
                    + "/contents/" + path;

            Map<String, Object> params = new HashMap<>();
            params.put("access_token", appProperties.getGitee().getAccessToken());
            params.put("message", "upload: " + filename);
            params.put("content", base64Content);

            HttpResponse response = HttpRequest.post(url)
                    .form(params)
                    .execute();

            if (!response.isOk()) {
                log.error("上传文件到Gitee失败: status={}, body={}", response.getStatus(), response.body());
                throw new RuntimeException("文件上传失败");
            }

            return getFileUrl(filename);
        } catch (IOException e) {
            log.error("读取文件内容失败", e);
            throw new RuntimeException("文件上传失败");
        }
    }

    @Override
    public void deleteFile(String filename) {
        String path = appProperties.getGitee().getPath() + "/" + filename;
        String sha = getFileSha(path);
        if (StrUtil.isBlank(sha)) {
            log.warn("获取文件SHA失败，跳过删除: {}", filename);
            return;
        }

        String url = appProperties.getGitee().getBaseUrl()
                + "/repos/" + appProperties.getGitee().getOwner()
                + "/" + appProperties.getGitee().getRepo()
                + "/contents/" + path;

        Map<String, Object> params = new HashMap<>();
        params.put("access_token", appProperties.getGitee().getAccessToken());
        params.put("message", "delete: " + filename);
        params.put("sha", sha);

        HttpResponse response = HttpRequest.delete(url)
                .form(params)
                .execute();

        if (!response.isOk()) {
            log.warn("删除Gitee文件失败: status={}, body={}", response.getStatus(), response.body());
        }
    }

    @Override
    public String getFileUrl(String filename) {
        return appProperties.getGitee().getRawUrl()
                + "/" + appProperties.getGitee().getOwner()
                + "/" + appProperties.getGitee().getRepo()
                + "/raw/" + appProperties.getGitee().getPath()
                + "/" + filename;
    }

    private String getFileSha(String path) {
        String url = appProperties.getGitee().getBaseUrl()
                + "/repos/" + appProperties.getGitee().getOwner()
                + "/" + appProperties.getGitee().getRepo()
                + "/contents/" + path
                + "?access_token=" + appProperties.getGitee().getAccessToken();

        try {
            HttpResponse response = HttpRequest.get(url).execute();
            if (response.isOk()) {
                JSONObject json = JSONUtil.parseObj(response.body());
                return json.getStr("sha");
            }
        } catch (Exception e) {
            log.warn("获取文件SHA失败: {}", e.getMessage());
        }
        return null;
    }
}
