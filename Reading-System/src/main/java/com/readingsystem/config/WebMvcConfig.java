package com.readingsystem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * 静态资源映射：/uploads/** -> 本地目录
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${upload.path:./uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = new File(uploadPath).getAbsolutePath();
        if (!location.endsWith(File.separator)) {
            location += File.separator;
        }
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + location);
    }
}
