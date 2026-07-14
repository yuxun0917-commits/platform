package com.platform.starter.file.local;

import cn.hutool.core.util.StrUtil;
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
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 本地磁盘存储实现
 *
 * <p>将文件写入 {@link SysStorageConfig#getBasePath()} 指定的本地目录（按日期分目录），
 * 访问地址由 {@link SysStorageConfig#getDomain()} 前缀拼接存储键得到。</p>
 *
 * @author platform
 */
@Slf4j
public class LocalFileStorage implements FileStorage {

    private final SysStorageConfig config;

    public LocalFileStorage(SysStorageConfig config) {
        this.config = config;
    }

    @Override
    public StorageTypeEnum type() {
        return StorageTypeEnum.LOCAL;
    }

    @Override
    public FileUploadResult upload(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String ext = getExtension(originalFilename);
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "/";
        String newFileName = UUID.randomUUID().toString().replace("-", "") + (ext.isEmpty() ? "" : "." + ext);
        String fileKey = datePath + newFileName;

        File dest = new File(config.getBasePath() + File.separator + datePath + newFileName);
        if (!dest.getParentFile().exists() && !dest.getParentFile().mkdirs()) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL, "创建存储目录失败");
        }
        try {
            file.transferTo(dest);
            log.info("[本地上传] 成功, originalName={}, size={}, key={}", originalFilename, file.getSize(), fileKey);
        } catch (IOException e) {
            log.error("[本地上传] 失败, originalName={}", originalFilename, e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL, "文件上传失败：" + e.getMessage());
        }
        return new FileUploadResult(fileKey, buildUrl(fileKey), config.getBucket());
    }

    @Override
    public FileUploadResult upload(File file, String originalFilename) {
        String ext = getExtension(originalFilename);
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")) + "/";
        String newFileName = UUID.randomUUID().toString().replace("-", "") + (ext.isEmpty() ? "" : "." + ext);
        String fileKey = datePath + newFileName;
        File dest = new File(config.getBasePath() + File.separator + datePath + newFileName);
        if (!dest.getParentFile().exists() && !dest.getParentFile().mkdirs()) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL, "创建存储目录失败");
        }
        try {
            Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            log.info("[本地上传] 成功(合并文件), name={}, size={}, key={}", originalFilename, file.length(), fileKey);
        } catch (IOException e) {
            log.error("[本地上传] 失败(合并文件), name={}", originalFilename, e);
            throw new BusinessException(ErrorCode.FILE_UPLOAD_FAIL, "文件上传失败：" + e.getMessage());
        }
        return new FileUploadResult(fileKey, buildUrl(fileKey), config.getBucket());
    }

    @Override
    public void delete(String fileKey) {
        if (StrUtil.isBlank(fileKey)) {
            return;
        }
        File dest = new File(config.getBasePath() + File.separator + fileKey);
        if (dest.exists() && !dest.delete()) {
            log.warn("[本地删除] 删除失败, path={}", dest.getAbsolutePath());
        }
    }

    @Override
    public String getAccessUrl(String fileKey) {
        return buildUrl(fileKey);
    }

    /**
     * 拼接访问地址：domain 前缀 + 存储键
     */
    private String buildUrl(String fileKey) {
        String domain = StrUtil.emptyToDefault(config.getDomain(), "/file/");
        String prefix = domain.endsWith("/") ? domain : domain + "/";
        return prefix + fileKey;
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
