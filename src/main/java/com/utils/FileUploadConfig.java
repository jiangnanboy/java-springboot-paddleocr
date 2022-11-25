package com.utils;

import ai.djl.training.dataset.Dataset;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;

import javax.servlet.MultipartConfigElement;

/**
 * @author sy
 * @date 2022/11/11 22:57
 */
@Configuration
public class FileUploadConfig {
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory multipartConfigFactory = new MultipartConfigFactory();
        // 单个文件大小为5MB
        multipartConfigFactory.setMaxFileSize(DataSize.ofMegabytes(5));
        // 总上传数据大小10MB
        multipartConfigFactory.setMaxRequestSize(DataSize.ofMegabytes(10));
        return multipartConfigFactory.createMultipartConfig();
    }
}











