package com.platform.starter.file.cos;

import cn.hutool.core.util.StrUtil;
import com.platform.common.entity.admin.SysStorageConfig;
import com.platform.common.enums.ErrorCode;
import com.platform.common.enums.StorageTypeEnum;
import com.platform.common.exception.BusinessException;
import com.platform.starter.file.FileStorage;
import com.platform.starter.file.FileUploadResult;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * 腾讯云 COS 存储实现
 *
 * @author platform
 */
@Slf4j
public class CosFileStorage implements FileStorage {

    private final SysStorageConfig config;

    public CosFileStorage(SysStorageConfig config) {
        this.config = config;
    }

    @Override
    public StorageTypeEnum type() {
        return StorageTypeEnum.COS;
    }

    @Override
    public FileUploadResult upload(MultipartFile file) {
        String ext = getExtension(file.getOriginalFilename());
        String fileKey = UUID.randomUUID().toString().replace("-", "") + (ext.isEmpty() ? "" : "." + ext);
        COSClient cosClient = buildClient();
        try (InputStream is = file.getInputStream()) {
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(file.getSize());
            meta.setContentType(StrUtil.emptyToDefault(file.getContentType(), "application/octet-stream"));
            cosClient.putObject(new PutObjectRequest(config.getBucket(), fileKey, is, meta));
            log.info("[COS上传] 成功, bucket={}, key={}", config.getBucket(), fileKey);
        } catch (IOException e) {
            log.error("[COS上传] 失败, bucket={}, key={}", config.getBucket(), fileKey, e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL, "COS上传失败：" + e.getMessage());
        } finally {
            cosClient.shutdown();
        }
        return new FileUploadResult(fileKey, buildUrl(fileKey), config.getBucket());
    }

    @Override
    public FileUploadResult upload(File file, String originalFilename) {
        String ext = getExtension(originalFilename);
        String fileKey = UUID.randomUUID().toString().replace("-", "") + (ext.isEmpty() ? "" : "." + ext);
        COSClient cosClient = buildClient();
        try {
            cosClient.putObject(new PutObjectRequest(config.getBucket(), fileKey, file));
            log.info("[COS上传] 成功(合并文件), bucket={}, key={}", config.getBucket(), fileKey);
        } finally {
            cosClient.shutdown();
        }
        return new FileUploadResult(fileKey, buildUrl(fileKey), config.getBucket());
    }

    @Override
    public void delete(String fileKey) {
        if (StrUtil.isBlank(fileKey)) {
            return;
        }
        COSClient cosClient = buildClient();
        try {
            cosClient.deleteObject(config.getBucket(), fileKey);
        } finally {
            cosClient.shutdown();
        }
    }

    @Override
    public String getAccessUrl(String fileKey) {
        return buildUrl(fileKey);
    }

    private COSClient buildClient() {
        COSCredentials cred = new BasicCOSCredentials(config.getAccessKey(), config.getSecretKey());
        ClientConfig clientConfig = new ClientConfig(new Region(config.getRegion()));
        return new COSClient(cred, clientConfig);
    }

    private String buildUrl(String fileKey) {
        if (StrUtil.isNotBlank(config.getDomain())) {
            String domain = config.getDomain().endsWith("/") ? config.getDomain() : config.getDomain() + "/";
            return domain + fileKey;
        }
        String endpoint = config.getEndpoint().replaceAll("^https?://", "");
        String region = StrUtil.emptyToDefault(config.getRegion(), "");
        return "https://" + config.getBucket() + ".cos." + region + "." + endpoint + "/" + fileKey;
    }

    private String getExtension(String filename) {
        if (StrUtil.isBlank(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
