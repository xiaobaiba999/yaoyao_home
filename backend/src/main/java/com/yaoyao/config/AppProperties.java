package com.yaoyao.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "yaoyao")
public class AppProperties {

    private Jwt jwt = new Jwt();
    private Admin admin = new Admin();
    private Gitee gitee = new Gitee();
    private Cors cors = new Cors();
    private String uploadDir = "./uploads";

    public boolean isGiteeConfigured() {
        return gitee.accessToken != null 
                && !gitee.accessToken.isEmpty()
                && !gitee.accessToken.startsWith("your_")
                && gitee.owner != null
                && !gitee.owner.isEmpty()
                && !gitee.owner.startsWith("your_")
                && gitee.repo != null
                && !gitee.repo.isEmpty()
                && !gitee.repo.startsWith("your_");
    }

    @Data
    public static class Jwt {
        private String secret = "yaoyao-memorial-secret-key-2024";
        private long expiresIn = 604800000L;
    }

    @Data
    public static class Admin {
        private String username = "yaoyao";
        private String password = "yaoyao2024";
    }

    @Data
    public static class Gitee {
        private String accessToken;
        private String owner;
        private String repo;
        private String baseUrl = "https://gitee.com/api/v5";
        private String rawUrl = "https://gitee.com";
        private String path = "images";
    }

    @Data
    public static class Cors {
        private String allowedOrigins = "http://localhost:5173";
    }
}
