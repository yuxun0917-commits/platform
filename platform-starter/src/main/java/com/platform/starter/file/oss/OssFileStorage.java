package com.platform.starter.file.oss;

import cn.hutool.core.util.StrUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectRequest;
import com.platform.common.entity.admin.SysStorageConfig;
import com.platform.common.enums.ErrorCode;
import com.platform.common.enums.StorageTypeEnum;
import com.platform.common.exception.BusinessException;
import com.platform.starter.file.FileStorage;
import com.platform.starter.file.FileUploadResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * 阿里云 OSS 存储实现
 *
 * @author platform
 */
@Slf4j
public class OssFileStorage implements FileStorage {

    private final SysStorageConfig config;

    public OssFileStorage(SysStorageConfig config) {
        this.config = config;
    }

    @Override
    public StorageTypeEnum type() {
        return StorageTypeEnum.OSS;
    }

    @Override
    public FileUploadResult upload(MultipartFile file) {
        String ext = getExtension(file.getOriginalFilename());
        String fileKey = UUID.randomUUID().toString().replace("-", "") + (ext.isEmpty() ? "" : "." + ext);
        OSS ossClient = buildClient();
        try (InputStream is = file.getInputStream()) {
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(file.getSize());
            meta.setContentType(StrUtil.emptyToDefault(file.getContentType(), "application/octet-stream"));
            ossClient.putObject(new PutObjectRequest(config.getBucket(), fileKey, is, meta));
            log.info("[OSS上传] 成功, bucket={}, key={}", config.getBucket(), fileKey);
        } catch (IOException e) {
            log.error("[OSS上传] 失败, bucket={}, key={}", config.getBucket(), fileKey, e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL, "OSS上传失败：" + e.getMessage());
        } finally {
            ossClient.shutdown();
        }
        return new FileUploadResult(fileKey, buildUrl(fileKey), config.getBucket());
    }

    @Override
    public FileUploadResult upload(File file, String originalFilename) {
        String ext = getExtension(originalFilename);
        String fileKey = UUID.randomUUID().toString().replace("-", "") + (ext.isEmpty() ? "" : "." + ext);
        OSS ossClient = buildClient();
        try {
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(file.length());
            ossClient.putObject(new PutObjectRequest(config.getBucket(), fileKey, file, meta));
            log.info("[OSS上传] 成功(合并文件), bucket={}, key={}", config.getBucket(), fileKey);
        } finally {
            ossClient.shutdown();
        }
        return new FileUploadResult(fileKey, buildUrl(fileKey), config.getBucket());
    }

    @Override
    public void delete(String fileKey) {
        if (StrUtil.isBlank(fileKey)) {
            return;
        }
        OSS ossClient = buildClient();
        try {
            ossClient.deleteObject(config.getBucket(), fileKey);
        } finally {
            ossClient.shutdown();
        }
    }

    @Override
    public String getAccessUrl(String fileKey) {
        return buildUrl(fileKey);
    }

    private OSS buildClient() {
        return new OSSClientBuilder().build(config.getEndpoint(), config.getAccessKey(), config.getSecretKey());
    }

    private String buildUrl(String fileKey) {
        if (StrUtil.isNotBlank(config.getDomain())) {
            String domain = config.getDomain().endsWith("/") ? config.getDomain() : config.getDomain() + "/";
            return domain + fileKey;
        }
        String endpoint = config.getEndpoint().replaceAll("^https?://", "");
        return "https://" + config.getBucket() + "." + endpoint + "/" + fileKey;
    }

    private String getExtension(String filename) {
        if (StrUtil.isBlank(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
