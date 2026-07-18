package com.platform.starter.file.minio;

import cn.hutool.core.util.StrUtil;
import com.platform.common.entity.admin.SysStorageConfig;
import com.platform.common.enums.ErrorCode;
import com.platform.common.enums.StorageTypeEnum;
import com.platform.common.exception.BusinessException;
import com.platform.starter.file.FileStorage;
import com.platform.starter.file.FileUploadResult;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import cn.hutool.core.io.IoUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.UUID;

/**
 * MinIO 存储实现（S3 兼容）
 *
 * @author platform
 */
@Slf4j
public class MinioFileStorage implements FileStorage {

    private final SysStorageConfig config;

    public MinioFileStorage(SysStorageConfig config) {
        this.config = config;
    }

    @Override
    public StorageTypeEnum type() {
        return StorageTypeEnum.MINIO;
    }

    @Override
    public FileUploadResult upload(MultipartFile file) {
        String ext = getExtension(file.getOriginalFilename());
        String fileKey = UUID.randomUUID().toString().replace("-", "") + (ext.isEmpty() ? "" : "." + ext);
        MinioClient client = buildClient();
        try (InputStream is = file.getInputStream()) {
            client.putObject(PutObjectArgs.builder()
                    .bucket(config.getBucket())
                    .object(fileKey)
                    .stream(is, file.getSize(), -1)
                    .contentType(StrUtil.emptyToDefault(file.getContentType(), "application/octet-stream"))
                    .build());
            log.info("[MinIO上传] 成功, bucket={}, key={}", config.getBucket(), fileKey);
        } catch (Exception e) {
            log.error("[MinIO上传] 失败, bucket={}, key={}", config.getBucket(), fileKey, e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL, "MinIO上传失败：" + e.getMessage());
        }
        return new FileUploadResult(fileKey, buildUrl(fileKey), config.getBucket());
    }

    @Override
    public FileUploadResult upload(File file, String originalFilename) {
        String ext = getExtension(originalFilename);
        String fileKey = UUID.randomUUID().toString().replace("-", "") + (ext.isEmpty() ? "" : "." + ext);
        MinioClient client = buildClient();
        try (InputStream is = new FileInputStream(file)) {
            client.putObject(PutObjectArgs.builder()
                    .bucket(config.getBucket())
                    .object(fileKey)
                    .stream(is, file.length(), -1)
                    .contentType(StrUtil.emptyToDefault(getContentType(ext), "application/octet-stream"))
                    .build());
            log.info("[MinIO上传] 成功(合并文件), bucket={}, key={}", config.getBucket(), fileKey);
        } catch (Exception e) {
            log.error("[MinIO上传] 失败(合并文件), bucket={}, key={}", config.getBucket(), fileKey, e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL, "MinIO上传失败：" + e.getMessage());
        }
        return new FileUploadResult(fileKey, buildUrl(fileKey), config.getBucket());
    }

    @Override
    public void delete(String fileKey) {
        if (StrUtil.isBlank(fileKey)) {
            return;
        }
        try {
            buildClient().removeObject(RemoveObjectArgs.builder()
                    .bucket(config.getBucket())
                    .object(fileKey)
                    .build());
        } catch (Exception e) {
            log.error("[MinIO删除] 失败, bucket={}, key={}", config.getBucket(), fileKey, e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL, "MinIO删除失败：" + e.getMessage());
        }
    }

    @Override
    public void download(String fileKey, OutputStream out) {
        if (StrUtil.isBlank(fileKey)) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL, "文件不存在");
        }
        MinioClient client = buildClient();
        try (InputStream in = client.getObject(GetObjectArgs.builder()
                .bucket(config.getBucket()).object(fileKey).build())) {
            IoUtil.copy(in, out);
            log.info("[MinIO下载] 成功, bucket={}, key={}", config.getBucket(), fileKey);
        } catch (Exception e) {
            log.error("[MinIO下载] 失败, bucket={}, key={}", config.getBucket(), fileKey, e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL, "MinIO文件读取失败：" + e.getMessage());
        }
    }

    @Override
    public String getAccessUrl(String fileKey) {
        return buildUrl(fileKey);
    }

    private MinioClient buildClient() {
        return MinioClient.builder()
                .endpoint(config.getEndpoint())
                .credentials(config.getAccessKey(), config.getSecretKey())
                .build();
    }

    private String buildUrl(String fileKey) {
        String ep = config.getEndpoint();
        if (!ep.startsWith("http")) {
            ep = (Objects.equals(config.getIsHttps(), 1) ? "https://" : "http://") + ep;
        }
        String base = ep.endsWith("/") ? ep.substring(0, ep.length() - 1) : ep;
        return base + "/" + config.getBucket() + "/" + fileKey;
    }

    private String getExtension(String filename) {
        if (StrUtil.isBlank(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    private String getContentType(String ext) {
        return switch (ext.toLowerCase()) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "bmp" -> "image/bmp";
            case "pdf" -> "application/pdf";
            case "txt" -> "text/plain";
            case "csv" -> "text/csv";
            case "zip" -> "application/zip";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "ppt" -> "application/vnd.ms-powerpoint";
            case "pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            default -> "application/octet-stream";
        };
    }
}
