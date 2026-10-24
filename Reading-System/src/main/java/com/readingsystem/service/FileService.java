package com.readingsystem.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

    /**
     * 上传文件，返回可访问 URL 路径(/uploads/...)
     */
    String upload(MultipartFile file);
}
