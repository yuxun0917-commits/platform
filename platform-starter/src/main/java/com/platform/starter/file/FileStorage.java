package com.platform.starter.file;

import com.platform.common.entity.admin.SysStorageConfig;
import com.platform.common.enums.StorageTypeEnum;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * 文件存储接口
 *
 * <p>统一抽象本地磁盘、阿里云OSS、腾讯云COS、MinIO 等多种存储后端。
 * 各实现由 {@link FileStorageFactory} 基于 {@link SysStorageConfig} 构建，
 * 由 {@code FileStorageManager} 按存储类型路由。</p>
 *
 * @author platform
 */
public interface FileStorage {

    /**
     * 存储类型
     *
     * @return 当前实现对应的存储类型枚举
     */
    StorageTypeEnum type();

    /**
     * 上传文件
     *
     * @param file 前端上传的文件
     * @return 上传结果（存储键、访问地址、桶名）
     */
    FileUploadResult upload(MultipartFile file);

    /**
     * 上传已落盘的文件（用于分片合并后上传等场景）
     *
     * @param file            已落盘的文件
     * @param originalFilename 原始文件名（用于推导扩展名）
     * @return 上传结果（存储键、访问地址、桶名）
     */
    FileUploadResult upload(File file, String originalFilename);

    /**
     * 删除文件
     *
     * @param fileKey 存储键（本地为相对路径，对象存储为 object key）
     */
    void delete(String fileKey);

    /**
     * 根据存储键获取访问地址
     *
     * @param fileKey 存储键
     * @return 访问地址
     */
    String getAccessUrl(String fileKey);
}
