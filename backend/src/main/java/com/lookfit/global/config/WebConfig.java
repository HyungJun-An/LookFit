package com.lookfit.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${fitting.image.upload-dir:src/main/resources/static/images/fitting/user}")
    private String uploadDir;

    @Value("${fitting.image.result-dir:src/main/resources/static/images/fitting/result}")
    private String resultDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 전체 images 디렉토리를 매핑 (간단하게)
        String imagesPath = "file:" + Paths.get(uploadDir)
                .toAbsolutePath()
                .getParent()  // user 제거
                .getParent()  // fitting 제거
                .toString() + "/";

        System.out.println("🖼️ Images path: " + imagesPath);

        registry.addResourceHandler("/images/**")
                .addResourceLocations(imagesPath)
                .addResourceLocations("classpath:/static/images/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                    "http://localhost:5173",  // Vite default port
                    "http://localhost:5174",  // Vite alternative port
                    "http://localhost:3000"   // React default port (backup)
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);  // Preflight cache duration (1 hour)
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // UTF-8 encoding for HTTP responses
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        stringConverter.setWriteAcceptCharset(false);
        converters.add(0, stringConverter);
    }
}
