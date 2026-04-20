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

    public Jwt getJwt() {
        return jwt;
    }

    public void setJwt(Jwt jwt) {
        this.jwt = jwt;
    }

    public Admin getAdmin() {
        return admin;
    }

    public void setAdmin(Admin admin) {
        this.admin = admin;
    }

    public Gitee getGitee() {
        return gitee;
    }

    public void setGitee(Gitee gitee) {
        this.gitee = gitee;
    }

    public Cors getCors() {
        return cors;
    }

    public void setCors(Cors cors) {
        this.cors = cors;
    }

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

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

        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        public long getExpiresIn() { return expiresIn; }
        public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
    }

    @Data
    public static class Admin {
        private String username = "yaoyao";
        private String password = "yaoyao2024";

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    @Data
    public static class Gitee {
        private String accessToken;
        private String owner;
        private String repo;
        private String baseUrl = "https://gitee.com/api/v5";
        private String rawUrl = "https://gitee.com";
        private String path = "images";

        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
        public String getOwner() { return owner; }
        public void setOwner(String owner) { this.owner = owner; }
        public String getRepo() { return repo; }
        public void setRepo(String repo) { this.repo = repo; }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getRawUrl() { return rawUrl; }
        public void setRawUrl(String rawUrl) { this.rawUrl = rawUrl; }
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
    }

    @Data
    public static class Cors {
        private String allowedOrigins = "http://localhost:5173";

        public String getAllowedOrigins() { return allowedOrigins; }
        public void setAllowedOrigins(String allowedOrigins) { this.allowedOrigins = allowedOrigins; }
    }
}
