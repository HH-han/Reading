package com.readingsystem.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.readingsystem.exception.BusinessException;
import com.readingsystem.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * 文件上传服务
 */
@Service
public class FileServiceImpl implements FileService {

    private static final long MAX_SIZE = 20L * 1024 * 1024;

    private static final Set<String> ALLOW_EXT = Set.of(
            "jpg", "jpeg", "png", "gif", "webp", "mp3", "m4a", "wav", "aac", "mp4", "mov");

    @Value("${upload.path:./uploads}")
    private String uploadPath;

    @Override
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException("文件大小不能超过20MB");
        }
        String original = file.getOriginalFilename();
        String ext = FileUtil.extName(original);
        if (StrUtil.isBlank(ext) || !ALLOW_EXT.contains(ext.toLowerCase())) {
            throw new BusinessException("不支持的文件类型");
        }
        String monthDir = DateUtil.format(DateUtil.date(), "yyyyMM");
        String fileName = IdUtil.simpleUUID() + "." + ext.toLowerCase();
        File dir = new File(uploadPath, monthDir);
        FileUtil.mkdir(dir);
        File dest = new File(dir, fileName);
        try {
            file.transferTo(dest.getAbsoluteFile());
        } catch (IOException e) {
            throw new BusinessException("文件保存失败");
        }
        return "/uploads/" + monthDir + "/" + fileName;
    }
}
