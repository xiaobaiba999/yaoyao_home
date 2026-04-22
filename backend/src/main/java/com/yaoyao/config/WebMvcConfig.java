package com.yaoyao.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AppProperties appProperties;
    private final JwtAuthInterceptor jwtAuthInterceptor;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = appProperties.getCors().getAllowedOrigins().split(",");
        registry.addMapping("/api/**")
                .allowedOriginPatterns(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(86400);
        registry.addMapping("/uploads/**")
                .allowedOriginPatterns(origins)
                .allowedMethods("GET")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(86400);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadDir = appProperties.getUploadDir();
        if (uploadDir != null && !uploadDir.isEmpty()) {
            registry.addResourceHandler("/uploads/**")
                    .addResourceLocations("file:" + uploadDir + "/")
                    .setCachePeriod(300)
                    .resourceChain(true)
                    .addResolver(new PathResourceResolver());
        }
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/me",
                        "/api/health",
                        "/api/messages",
                        "/api/diaries",
                        "/api/wishes",
                        "/api/plans",
                        "/api/timeline",
                        "/api/photos",
                        "/api/photos/download/**",
                        "/api/photos/serve/**",
                        "/api/upload",
                        "/api/music/**"
                );
    }
}
