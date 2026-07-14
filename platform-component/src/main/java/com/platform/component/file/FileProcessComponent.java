package com.platform.component.file;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.platform.common.entity.admin.SysStorageConfig;
import com.platform.common.enums.ErrorCode;
import com.platform.common.exception.BusinessException;
import com.platform.starter.file.FileStorageManager;
import com.platform.starter.file.FileUploadProperties;
import com.platform.starter.file.FileUploadResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 文件处理组件
 *
 * <p>封装跨模块的文件上传/删除聚合逻辑：先按全局配置做大小与类型校验，
 * 再交由 {@link FileStorageManager} 根据 {@link SysStorageConfig} 选择具体存储后端执行。
 * 供 Controller 层直接调用，返回的结果包含写入 {@code sys_attachment} 所需的元数据。</p>
 *
 * @author platform
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileProcessComponent {

    private final FileStorageManager fileStorageManager;
    private final FileUploadProperties fileUploadProperties;

    /**
     * 单文件上传（按指定存储配置）
     *
     * @param file   上传文件
     * @param config 存储配置
     * @return 上传结果（存储键、访问地址、桶名）
     */
    public FileUploadResult upload(MultipartFile file, SysStorageConfig config) {
        validate(file);
        return fileStorageManager.getStorage(config).upload(file);
    }

    /**
     * 批量上传（按指定存储配置）
     *
     * @param files  文件列表
     * @param config 存储配置
     * @return 上传结果列表
     */
    public List<FileUploadResult> batchUpload(List<MultipartFile> files, SysStorageConfig config) {
        List<FileUploadResult> results = new ArrayList<>();
        if (CollUtil.isEmpty(files)) {
            return results;
        }
        for (MultipartFile file : files) {
            results.add(upload(file, config));
        }
        return results;
    }

    /**
     * 删除文件（按指定存储配置）
     *
     * @param config  存储配置
     * @param fileKey 存储键
     */
    public void delete(SysStorageConfig config, String fileKey) {
        fileStorageManager.getStorage(config).delete(fileKey);
    }

    /**
     * 文件大小与类型校验（基于全局 file.upload 配置）
     *
     * @param file 上传文件
     */
    private void validate(MultipartFile file) {
        if (Objects.isNull(file) || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL, "上传文件不能为空");
        }
        long maxBytes = fileUploadProperties.getMaxSize() * 1024L * 1024L;
        if (file.getSize() > maxBytes) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEED,
                    "文件大小超出限制，最大允许 " + fileUploadProperties.getMaxSize() + "MB");
        }
        String ext = getExtension(file.getOriginalFilename());
        if (!fileUploadProperties.getAllowedTypes().contains(ext.toLowerCase())) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED,
                    "文件类型不允许，支持的类型：" + fileUploadProperties.getAllowedTypes());
        }
    }

    /**
     * 获取文件扩展名（不含点）
     */
    private String getExtension(String filename) {
        if (StrUtil.isBlank(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
